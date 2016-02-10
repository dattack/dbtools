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

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import javax.naming.Binding;
import javax.naming.CannotProceedException;
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
import javax.naming.OperationNotSupportedException;

import org.apache.commons.lang.StringUtils;

/**
 * Provides the base implementation of naming context.
 *
 * @author cvarela
 * @since 0.1
 */
public abstract class AbstractContext implements Cloneable, Context {

    private boolean closing;

    // the environment properties
    private Hashtable<Object, Object> env; // NOPMD by cvarela on 8/02/16 22:32

    private Name nameInNamespace;

    private final NameParser nameParser;

    // the direct subcontext
    private Hashtable<Name, Context> subContexts = new Hashtable<Name, Context>(); // NOPMD by cvarela on 8/02/16

    // the binded table
    private Map<Name, Object> objectTable = new Hashtable<Name, Object>();

    protected AbstractContext(final AbstractContext that) throws NamingException {
        this(that.env);
    }

    protected AbstractContext(final Hashtable<?, ?> env) throws NamingException { // NOPMD by cvarela on 8/02/16 22:31

        this.env = new Hashtable<Object, Object>();
        if (env != null) {
            this.env.putAll(env);
        }
        this.closing = false;
        nameParser = new DefaultNameParser(this);
        nameInNamespace = nameParser.parse("");
    }

    @Override
    public Object addToEnvironment(final String name, final Object object) throws NamingException {
        return this.env.put(name, object);
    }

    @Override
    public void bind(final Name name, final Object object) throws NamingException {

        ensureContextNotClosed();

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

        if (objectTable.containsKey(name) || subContexts.containsKey(name) || env.containsKey(name.toString())) {
            throw new NameAlreadyBoundException(
                    String.format("Name %s already bound. Use rebind() to override", name.toString()));
        }

        if (object instanceof Context) {
            subContexts.put(name, (Context) object);
        } else {
            objectTable.put(name, object);
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

        synchronized (this) {
            if (closing) {
                return;
            }
            this.closing = true;

            // close all subcontext
            destroySubcontexts();

            // release binded objects
            this.objectTable.clear();
            this.objectTable = null;
            this.subContexts = null;
            this.env = null;
        }
    }

    @Override
    public Name composeName(final Name name, final Name prefix) throws NamingException {

        if (name == null || prefix == null) {
            throw new InvalidNameException(
                    String.format("Unable to compose name with null values (prefix: %s, name: %s)", prefix, name));
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
    public Context createSubcontext(final Name name) throws NamingException {

        ensureContextNotClosed();
        if (closing) {
            throw new CannotProceedException("Context is closed");
        }
        return doCreateSubcontext(name);
    }

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

        if (objectTable.containsKey(name) || !subContexts.containsKey(name)) {
            throw new NameNotFoundException(String.format("Context not found: %s", name));
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

    private void destroySubcontexts() throws NamingException {
        for (final Name name : subContexts.keySet()) {
            destroySubcontext(name);
        }
    }

    /**
     * Creates a new subcontext.
     * 
     * @param name
     *            the name of the context to create
     * @return the subcontext
     * @throws NamingException
     *             if an error occurs
     */
    public abstract Context doCreateSubcontext(Name name) throws NamingException;

    private void ensureContextNotClosed() throws NamingException {
        if (closing) {
            throw new CannotProceedException("Context is closed");
        }
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

    private Context getParentContext(final Name name) throws NamingException {

        final Object context = lookup(name.getPrefix(name.size() - 1));
        if (context instanceof Context) {
            return (Context) context;
        }
        throw new NamingException(String.format("Cannot unbind object. Target context does not exist (%s)",
                name.getPrefix(name.size() - 1)));
    }

    /**
     * Returns the subcontexts of this context.
     *
     * @return the subcontexts of this context.
     */
    @SuppressWarnings("unchecked")
    protected Map<Name, Object> getSubContexts() {
        return (Hashtable<Name, Object>) subContexts.clone();
    }

    @Override
    public NamingEnumeration<NameClassPair> list(final Name name) throws NamingException {

        ensureContextNotClosed();

        if (name == null || name.isEmpty()) {
            // list all elements
            final Map<Name, Object> enumStore = new HashMap<Name, Object>();
            enumStore.putAll(objectTable);
            enumStore.putAll(subContexts);
            return new NameClassPairNamingEnumeration(enumStore);
        }

        final Name prefixName = name.getPrefix(1);
        if (objectTable.containsKey(prefixName)) {
            throw new NotContextException(String.format("%s cannot be listed", name));
        }

        if (subContexts.containsKey(prefixName)) {
            return subContexts.get(prefixName).list(name.getSuffix(1));
        }

        throw new NamingException(String.format("The context '%s' can't be found", name));
    }

    @Override
    public NamingEnumeration<NameClassPair> list(final String name) throws NamingException {
        return list(nameParser.parse(name));
    }

    @Override
    public NamingEnumeration<Binding> listBindings(final Name name) throws NamingException {

        ensureContextNotClosed();

        if (name == null || name.isEmpty()) {
            final Map<Name, Object> enumStore = new HashMap<Name, Object>();
            enumStore.putAll(objectTable);
            enumStore.putAll(subContexts);
            return new BindingNamingEnumeration(enumStore);
        }

        final Name subName = name.getPrefix(1);

        if (objectTable.containsKey(subName)) {
            throw new NotContextException(String.format("%s cannot be listed", name));
        }

        if (subContexts.containsKey(subName)) {
            return subContexts.get(subName).listBindings(name.getSuffix(1));
        }

        throw new NamingException(String.format("The named context '%s' can't be found", name));
    }

    @Override
    public NamingEnumeration<Binding> listBindings(final String name) throws NamingException {
        return listBindings(nameParser.parse(name));
    }

    @Override
    public Object lookup(final Name name) throws NamingException {

        ensureContextNotClosed();

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
                throw (NamingException) new OperationNotSupportedException(e.getMessage()).initCause(e);
            }
        }

        final Name objName = name.getPrefix(1);
        if (name.size() > 1) {
            if (subContexts.containsKey(objName)) {
                return subContexts.get(objName).lookup(name.getSuffix(1));
            }
            throw new NamingException(
                    String.format("Invalid subcontext '%s' in context '%s'", objName.toString(), 
                            StringUtils.isBlank(getNameInNamespace()) ? "/" : getNameInNamespace()));
        }

        if (objectTable.containsKey(name)) {
            return objectTable.get(objName);
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

        ensureContextNotClosed();

        if (name.isEmpty()) {
            throw new InvalidNameException("Cannot rebind to empty name");
        }

        // the parent context must exists
        getParentContext(name);
        // final Object targetContext = lookup(name.getPrefix(name.size() - 1));
        // if (targetContext == null || !(targetContext instanceof Context)) {
        // throw new NamingException(
        // String.format("Cannot bind object due context does not exist (%s)", name.toString()));
        // }
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

        ensureContextNotClosed();

        if (newName.isEmpty()) {
            throw new InvalidNameException("Cannot bind to empty name");
        }

        final Object oldValue = lookup(oldName);
        if (oldValue == null) {
            throw new NamingException(String.format("Cannot rename object: name not found (%s)", oldName));
        }

        if (lookup(newName) != null) {
            throw new NameAlreadyBoundException(
                    String.format("Cannot rename object: name already bound (%s)", newName));
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

        ensureContextNotClosed();

        if (name.isEmpty()) {
            throw new InvalidNameException("Cannot unbind to empty name");
        }

        if (name.size() == 1) {
            if (objectTable.containsKey(name)) {
                objectTable.remove(name);
            }
            return;
        }

        final Context parentContext = getParentContext(name);

        parentContext.unbind(name.getSuffix(name.size() - 1));
    }

    @Override
    public void unbind(final String name) throws NamingException {
        unbind(nameParser.parse(name));
    }
}
