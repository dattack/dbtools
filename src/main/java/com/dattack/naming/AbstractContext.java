/*
 * Copyright (c) 2014, The Dattack team (http://www.dattack.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.dattack.naming;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.naming.Binding;
import javax.naming.Context;
import javax.naming.ContextNotEmptyException;
import javax.naming.InvalidNameException;
import javax.naming.Name;
import javax.naming.NameAlreadyBoundException;
import javax.naming.NameClassPair;
import javax.naming.NameNotFoundException;
import javax.naming.NameParser;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.NotContextException;

/**
 * Provides the base implementation of naming context.
 *
 * @author cvarela
 * @since 0.1
 */
public abstract class AbstractContext implements Cloneable, Context {

    private boolean closing;
    private Hashtable<String, Object> env; // NOPMD by cvarela on 8/02/16 22:32

    private Name nameInNamespace;

    private NameParser nameParser;
    private final Hashtable<Name, Context> subContexts = new Hashtable<Name, Context>(); // NOPMD by cvarela on 8/02/16
    private Hashtable<Name, Object> table = new Hashtable<Name, Object>(); // NOPMD by cvarela on 8/02/16 22:32

    protected AbstractContext(final AbstractContext that) {
        this(that.env);
    }

    protected AbstractContext(final Hashtable<?, ?> env) { // NOPMD by cvarela on 8/02/16 22:31
        this(env, null);
    }

    private AbstractContext(final Hashtable<?, ?> env, final NameParser parser) { // NOPMD by cvarela on 8/02/16 22:32

        if (parser == null) {
            try {
                this.env = new Hashtable<String, Object>();
                for (final Entry<?, ?> entry : env.entrySet()) {
                    this.env.put(entry.getKey().toString(), entry.getValue());
                }
                nameParser = new DefaultNameParser(this);
            } catch (final NamingException e) {
                e.printStackTrace();
            }
        }

        try {
            nameInNamespace = nameParser.parse("");
        } catch (final NamingException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Object addToEnvironment(final String name, final Object object) throws NamingException {
        if (this.env == null) {
            return null;
        }
        return this.env.put(name, object);
    }

    @Override
    public void bind(final Name name, final Object object) throws NamingException {

        if (object == null) {
            return;
        }

        if (name.isEmpty()) {
            throw new InvalidNameException("Cannot bind to an empty name");
        }

        final Name prefix = name.getPrefix(1);
        if (subContexts.containsKey(prefix)) {
            subContexts.get(prefix).bind(name.getSuffix(1), object);
            return;
        }

        if (table.containsKey(name) || subContexts.containsKey(name) || env.containsKey(name.toString())) {
            throw new NameAlreadyBoundException("Name " + name.toString() //
                    + " already bound.  Use rebind() to override");
        }

        if (object instanceof Context) {
            subContexts.put(name, (Context) object);
        } else {
            table.put(name, object);
        }
    }

    @Override
    public void bind(final String name, final Object object) throws NamingException {
        bind(nameParser.parse(name), object);
    }

    @Override
    public void close() throws NamingException {
        if (closing) {
            return;
        }

        for (final Name name : subContexts.keySet()) {
            destroySubcontext(name);
        }

        while (!table.isEmpty() || !subContexts.isEmpty()) {
            final List<Name> toRemove = new LinkedList<Name>();
            for (final Name name : table.keySet()) {

                final Object entry = table.get(name);

                if (entry instanceof Thread) {
                    final Thread thread = (Thread) entry;
                    if (thread.isAlive()) {
                        toRemove.add(name);
                    }
                } else {
                    toRemove.add(name);
                }
            }

            for (final Iterator<?> it = toRemove.iterator(); it.hasNext();) {
                table.remove(it.next());
            }

            toRemove.clear();

            for (final Name name : subContexts.keySet()) {
                final AbstractContext context = (AbstractContext) subContexts.get(name);
                if (context.isEmpty()) {
                    toRemove.add(name);
                }
            }

            for (final Object obj : toRemove) {
                subContexts.remove(obj);
            }
        }

        this.env = null;
        this.table = null;
    }

    @Override
    public Name composeName(final Name name, final Name prefix) throws NamingException {

        if (name == null || prefix == null) {
            throw new InvalidNameException(
                    "Unable to compose name with null values (prefix: " + prefix + ", name: " + name + ")");
        }

        final Name composeName = (Name) prefix.clone();
        composeName.addAll(name);
        return composeName;
    }

    @Override
    public String composeName(final String name, final String prefix) throws NamingException {
        return composeName(nameParser.parse(name), nameParser.parse(prefix)).toString();
    }

    @Override
    public abstract Context createSubcontext(Name name) throws NamingException;

    @Override
    public Context createSubcontext(final String name) throws NamingException {
        return createSubcontext(nameParser.parse(name));
    }

    @Override
    public void destroySubcontext(final Name name) throws NamingException {

        if (name.size() > 1) {
            if (subContexts.containsKey(name.getPrefix(1))) {
                final Context subContext = subContexts.get(name.getPrefix(1));
                subContext.destroySubcontext(name.getSuffix(1));
                return;
            }
            throw new NameNotFoundException();
        }

        if (table.containsKey(name)) {
            throw new NotContextException();
        }

        if (!subContexts.containsKey(name)) {
            throw new NameNotFoundException();
        }
        final Context subContext = subContexts.get(name);

        final NamingEnumeration<NameClassPair> names = subContext.list("");
        if (names.hasMore()) {
            throw new ContextNotEmptyException();
        }

        subContexts.get(name).close();
        subContexts.remove(name);
    }

    @Override
    public void destroySubcontext(final String name) throws NamingException {
        destroySubcontext(nameParser.parse(name));
    }

    @Override
    public Hashtable<?, ?> getEnvironment() throws NamingException {
        if (this.env == null) {
            return new Hashtable<String, Object>();
        }

        return (Hashtable<?, ?>) this.env.clone();
    }

    @Override
    public String getNameInNamespace() throws NamingException {
        return nameInNamespace.toString();
    }

    @Override
    public NameParser getNameParser(final Name name) throws NamingException {

        if (name == null || name.isEmpty() || (name.size() == 1 && name.toString().equals(getNameInNamespace()))) {
            return nameParser;
        }

        final Name subName = name.getPrefix(1);
        if (subContexts.containsKey(subName)) {
            return subContexts.get(subName).getNameParser(name.getSuffix(1));
        }

        throw new NotContextException();
    }

    @Override
    public NameParser getNameParser(final String name) throws NamingException {
        return getNameParser(nameParser.parse(name));
    }

    /**
     * Returns the subcontexts of this context.
     *
     * @return the subcontexts of this context.
     */
    @SuppressWarnings("unchecked")
    protected Hashtable<Name, Object> getSubContexts() { // NOPMD by cvarela on 8/02/16 22:30
        return (Hashtable<Name, Object>) subContexts.clone();
    }

    private boolean isEmpty() {
        return (table.isEmpty() && subContexts.isEmpty());
    }

    @Override
    public NamingEnumeration<NameClassPair> list(final Name name) throws NamingException {
        if (name == null || name.isEmpty()) {
            // list all elements
            final Map<Name, Object> enumStore = new HashMap<Name, Object>();
            enumStore.putAll(table);
            enumStore.putAll(subContexts);
            return new NameClassPairNamingEnumeration(enumStore);
        }

        final Name subName = name.getPrefix(1);
        if (table.containsKey(subName)) {
            throw new NotContextException(name + " cannot be listed");
        }

        if (subContexts.containsKey(subName)) {
            return subContexts.get(subName).list(name.getSuffix(1));
        }

        throw new NamingException("The named context '" + name + "' can't be found");
    }

    @Override
    public NamingEnumeration<NameClassPair> list(final String name) throws NamingException {
        return list(nameParser.parse(name));
    }

    @Override
    public NamingEnumeration<Binding> listBindings(final Name name) throws NamingException {
        if (name == null || name.isEmpty()) {
            final Map<Name, Object> enumStore = new HashMap<Name, Object>();
            enumStore.putAll(table);
            enumStore.putAll(subContexts);
            return new BindingNamingEnumeration(enumStore);
        }

        final Name subName = name.getPrefix(1);

        if (table.containsKey(subName)) {
            throw new NotContextException(name + " cannot be listed");
        }

        if (subContexts.containsKey(subName)) {
            return subContexts.get(subName).listBindings(name.getSuffix(1));
        }

        throw new NamingException("The named context '" + name + "' can't be found");
    }

    @Override
    public NamingEnumeration<Binding> listBindings(final String name) throws NamingException {
        return listBindings(nameParser.parse(name));
    }

    @Override
    public Object lookup(final Name name) throws NamingException {

        /*
         * Extract from Context Javadoc: If name is empty, returns a new instance of this context (which represents the
         * same naming context as this context, but its environment may be modified independently and it may be accessed
         * concurrently).
         */
        if (name.isEmpty()) {
            try {
                return this.clone();
            } catch (final CloneNotSupportedException e) {
                // this shouldn't happen, since we are Cloneable
                throw new InternalError(e.getMessage());
            }
        }

        final Name objName = name.getPrefix(1);
        if (name.size() > 1) {
            if (subContexts.containsKey(objName)) {
                return subContexts.get(objName).lookup(name.getSuffix(1));
            }
            throw new NamingException(MessageFormat.format("Invalid subcontext ''{0}'' in context ''{1}''",
                    objName.toString(), getNameInNamespace()));
        }

        if (table.containsKey(name)) {
            return table.get(objName);
        }

        if (subContexts.containsKey(name)) {
            return subContexts.get(name);
        }

        if (env.containsKey(name.toString())) {
            return env.get(name.toString());
        }

        // not found
        return null;
    }

    @Override
    public Object lookup(final String name) throws NamingException {
        return lookup(nameParser.parse(name));
    }

    @Override
    public Object lookupLink(final Name name) throws NamingException {
        return lookup(name);
    }

    @Override
    public Object lookupLink(final String name) throws NamingException {
        return lookup(nameParser.parse(name));
    }

    @Override
    public void rebind(final Name name, final Object object) throws NamingException {

        if (name.isEmpty()) {
            throw new InvalidNameException("Cannot bind to empty name");
        }

        final Object targetContext = lookup(name.getPrefix(name.size() - 1));
        if (targetContext == null || !(targetContext instanceof Context)) {
            throw new NamingException(
                    MessageFormat.format("Cannot bind object: context does not exist ({0})", name.toString()));
        }
        unbind(name);
        bind(name, object);
    }

    @Override
    public void rebind(final String name, final Object object) throws NamingException {
        rebind(nameParser.parse(name), object);
    }

    @Override
    public Object removeFromEnvironment(final String name) throws NamingException {
        if (this.env == null) {
            return null;
        }
        return this.env.remove(name);
    }

    @Override
    public void rename(final Name oldName, final Name newName) throws NamingException {

        if (newName.isEmpty()) {
            throw new InvalidNameException("Cannot bind to empty name");
        }

        final Object oldValue = lookup(oldName);
        if (oldValue == null) {
            throw new NamingException(MessageFormat.format("Cannot rename object: name not found ({0})", oldName));
        }

        if (lookup(newName) != null) {
            throw new NameAlreadyBoundException(
                    MessageFormat.format("Cannot rename object: name already bound ({0})", newName));
        }

        unbind(oldName);
        unbind(newName);
        bind(newName, oldValue);
    }

    @Override
    public void rename(final String oldName, final String newName) throws NamingException {
        rename(nameParser.parse(oldName), nameParser.parse(newName));
    }

    /**
     * Sets the full name of this context within its own namespace.
     *
     * @param name
     *            the context's name
     * @throws NamingException
     *             if a name already set
     */
    protected void setNameInNamespace(final Name name) throws NamingException {
        if (nameInNamespace != null && !nameInNamespace.isEmpty()) {
            throw new NamingException("Name already set.");
        }
        nameInNamespace = name;
    }

    @Override
    public void unbind(final Name name) throws NamingException {

        if (name.isEmpty()) {
            throw new InvalidNameException("Cannot unbind to empty name");
        }

        if (name.size() == 1) {
            if (table.containsKey(name)) {
                table.remove(name);
            }
            return;
        }

        final Object targetContext = lookup(name.getPrefix(name.size() - 1));
        if (targetContext == null || !(targetContext instanceof Context)) {
            throw new NamingException("Cannot unbind object. Target context does not exist.");
        }

        ((Context) targetContext).unbind(name.getSuffix(name.size() - 1));
    }

    @Override
    public void unbind(final String name) throws NamingException {
        unbind(nameParser.parse(name));
    }
}
