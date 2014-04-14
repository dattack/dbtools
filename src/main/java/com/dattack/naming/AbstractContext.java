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
	private Hashtable<String, Object> env;

	private Name nameInNamespace;

	private NameParser nameParser;
	private Hashtable<Name, Context> subContexts = new Hashtable<Name, Context>();
	private Hashtable<Name, Object> table = new Hashtable<Name, Object>();

	protected AbstractContext(AbstractContext that) {
		this(that.env);
	}

	protected AbstractContext(Hashtable<?, ?> env) {
		this(env, null);
	}

	private AbstractContext(Hashtable<?, ?> env, NameParser parser) {

		if (parser == null) {
			try {
				this.env = new Hashtable<String, Object>();
				for (Entry<?, ?> entry : env.entrySet()) {
					this.env.put(entry.getKey().toString(), entry.getValue());
				}
				nameParser = new DefaultNameParser(this);
			} catch (NamingException e) {
				e.printStackTrace();
			}
		}

		try {
			nameInNamespace = nameParser.parse("");
		} catch (NamingException e) {
			e.printStackTrace();
		}
	}

	/** {@inheritDoc} */
	public Object addToEnvironment(String name, Object object) throws NamingException {
		if (this.env == null) {
			return null;
		}
		return this.env.put(name, object);
	}

	/** {@inheritDoc} */
	public void bind(Name name, Object object) throws NamingException {

		if (object == null) {
			return;
		}

		if (name.isEmpty()) {
			throw new InvalidNameException("Cannot bind to an empty name");
		} else {
			Name prefix = name.getPrefix(1);
			if (subContexts.containsKey(prefix)) {
				subContexts.get(prefix).bind(name.getSuffix(1), object);
				return;
			}
		}

		if (table.containsKey(name) || subContexts.containsKey(name) || env.containsKey(name.toString())) {
			throw new NameAlreadyBoundException("Name " + name.toString() + " already bound.  Use rebind() to override");
		}

		if (object instanceof Context) {
			subContexts.put(name, (Context) object);
		} else {
			table.put(name, object);
		}
	}

	/** {@inheritDoc} */
	public void bind(String name, Object object) throws NamingException {
		bind(nameParser.parse(name), object);
	}

	/** {@inheritDoc} */
	public void close() throws NamingException {
		if (closing) {
			return;
		}

		for (Name name : subContexts.keySet()) {
			destroySubcontext(name);
		}

		while (table.size() > 0 || subContexts.size() > 0) {
			List<Name> toRemove = new LinkedList<Name>();
			for (Name name : table.keySet()) {

				Object entry = table.get(name);

				if (entry instanceof Thread) {
					Thread thread = (Thread) entry;
					if (thread.isAlive()) {
						toRemove.add(name);
					}
				} else {
					toRemove.add(name);
				}
			}

			for (Iterator<?> it = toRemove.iterator(); it.hasNext();) {
				table.remove(it.next());
			}

			toRemove.clear();

			for (Name name : subContexts.keySet()) {
				AbstractContext context = (AbstractContext) subContexts.get(name);
				if (context.isEmpty()) {
					toRemove.add(name);
				}
			}

			for (Object obj : toRemove) {
				subContexts.remove(obj);
			}
		}

		this.env = null;
		this.table = null;
	}

	/** {@inheritDoc} */
	public Name composeName(Name name, Name prefix) throws NamingException {

		if (name == null || prefix == null) {
			throw new InvalidNameException("Unable to compose name with null values (prefix: " + prefix + ", name: "
					+ name + ")");
		}

		Name composeName = (Name) prefix.clone();
		composeName.addAll(name);
		return composeName;
	}

	/** {@inheritDoc} */
	public String composeName(String name, String prefix) throws NamingException {
		return composeName(nameParser.parse(name), nameParser.parse(prefix)).toString();
	}

	/** {@inheritDoc} */
	public abstract Context createSubcontext(Name name) throws NamingException;

	/** {@inheritDoc} */
	public Context createSubcontext(String name) throws NamingException {
		return createSubcontext(nameParser.parse(name));
	}

	/** {@inheritDoc} */
	public void destroySubcontext(Name name) throws NamingException {

		if (name.size() > 1) {
			if (subContexts.containsKey(name.getPrefix(1))) {
				Context subContext = (Context) subContexts.get(name.getPrefix(1));
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
		Context subContext = (Context) subContexts.get(name);

		NamingEnumeration<NameClassPair> names = subContext.list("");
		if (names.hasMore()) {
			throw new ContextNotEmptyException();
		}

		subContexts.get(name).close();
		subContexts.remove(name);
	}

	/** {@inheritDoc} */
	public void destroySubcontext(String name) throws NamingException {
		destroySubcontext(nameParser.parse(name));
	}

	/** {@inheritDoc} */
	public Hashtable<?, ?> getEnvironment() throws NamingException {
		if (this.env == null) {
			return new Hashtable<String, Object>();
		}
		return (Hashtable<?, ?>) this.env.clone();
	}

	/** {@inheritDoc} */
	public String getNameInNamespace() throws NamingException {
		return nameInNamespace.toString();
	}

	/** {@inheritDoc} */
	public NameParser getNameParser(Name name) throws NamingException {

		if (name == null || name.isEmpty() || (name.size() == 1 && name.toString().equals(getNameInNamespace()))) {
			return nameParser;
		}

		Name subName = name.getPrefix(1);
		if (subContexts.containsKey(subName)) {
			return ((Context) subContexts.get(subName)).getNameParser(name.getSuffix(1));
		}

		throw new NotContextException();
	}

	/** {@inheritDoc} */
	public NameParser getNameParser(String name) throws NamingException {
		return getNameParser(nameParser.parse(name));
	}

	/**
	 * Returns the subcontexts of this context.
	 * 
	 * @return the subcontexts of this context.
	 */
	@SuppressWarnings("unchecked")
	protected Hashtable<Name, Object> getSubContexts() {
		return (Hashtable<Name, Object>) subContexts.clone();
	}

	private boolean isEmpty() {
		return (table.size() == 0 && subContexts.size() == 0);
	}

	/** {@inheritDoc} */
	public NamingEnumeration<NameClassPair> list(Name name) throws NamingException {
		if (name == null || name.isEmpty()) {
			// list all elements
			Map<Name, Object> enumStore = new HashMap<Name, Object>();
			enumStore.putAll(table);
			enumStore.putAll(subContexts);
			return new NameClassPairNamingEnumeration(enumStore);
		}

		Name subName = name.getPrefix(1);
		if (table.containsKey(subName)) {
			throw new NotContextException(name + " cannot be listed");
		}

		if (subContexts.containsKey(subName)) {
			return subContexts.get(subName).list(name.getSuffix(1));
		}

		throw new NamingException("The named context '" + name + "' can't be found");
	}

	/** {@inheritDoc} */
	public NamingEnumeration<NameClassPair> list(String name) throws NamingException {
		return list(nameParser.parse(name));
	}

	/** {@inheritDoc} */
	public NamingEnumeration<Binding> listBindings(Name name) throws NamingException {
		if (name == null || name.isEmpty()) {
			Map<Name, Object> enumStore = new HashMap<Name, Object>();
			enumStore.putAll(table);
			enumStore.putAll(subContexts);
			return new BindingNamingEnumeration(enumStore);
		}

		Name subName = name.getPrefix(1);

		if (table.containsKey(subName)) {
			throw new NotContextException(name + " cannot be listed");
		}

		if (subContexts.containsKey(subName)) {
			return ((Context) subContexts.get(subName)).listBindings(name.getSuffix(1));
		}

		throw new NamingException("The named context '" + name + "' can't be found");
	}

	/** {@inheritDoc} */
	public NamingEnumeration<Binding> listBindings(String name) throws NamingException {
		return listBindings(nameParser.parse(name));
	}

	/** {@inheritDoc} */
	public Object lookup(final Name name) throws NamingException {

		/*
		 * Extract from Context Javadoc: If name is empty, returns a new instance of this context (which represents the
		 * same naming context as this context, but its environment may be modified independently and it may be accessed
		 * concurrently).
		 */
		if (name.isEmpty()) {
			try {
				return this.clone();
			} catch (CloneNotSupportedException e) {
				// this shouldn't happen, since we are Cloneable
				throw new InternalError();
			}
		}

		Name objName = name.getPrefix(1);
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

	/** {@inheritDoc} */
	public Object lookup(String name) throws NamingException {
		return lookup(nameParser.parse(name));
	}

	/** {@inheritDoc} */
	public Object lookupLink(Name name) throws NamingException {
		return lookup(name);
	}

	/** {@inheritDoc} */
	public Object lookupLink(String name) throws NamingException {
		return lookup(nameParser.parse(name));
	}

	/** {@inheritDoc} */
	public void rebind(Name name, Object object) throws NamingException {

		if (name.isEmpty()) {
			throw new InvalidNameException("Cannot bind to empty name");
		}

		Object targetContext = lookup(name.getPrefix(name.size() - 1));
		if (targetContext == null || !(targetContext instanceof Context)) {
			throw new NamingException(MessageFormat.format("Cannot bind object: context does not exist ({0})",
					name.toString()));
		}
		unbind(name);
		bind(name, object);
	}

	/** {@inheritDoc} */
	public void rebind(String name, Object object) throws NamingException {
		rebind(nameParser.parse(name), object);
	}

	/** {@inheritDoc} */
	public Object removeFromEnvironment(String name) throws NamingException {
		if (this.env == null) {
			return null;
		}
		return this.env.remove(name);
	}

	/** {@inheritDoc} */
	public void rename(Name oldName, Name newName) throws NamingException {

		Object oldValue = lookup(oldName);
		if (newName.isEmpty()) {
			throw new InvalidNameException("Cannot bind to empty name");
		}

		if (oldValue == null) {
			throw new NamingException(MessageFormat.format("Cannot rename object: name not found ({0})", oldName));
		}

		if (lookup(newName) != null) {
			throw new NameAlreadyBoundException(MessageFormat.format("Cannot rename object: name already bound ({0})",
					newName));
		}

		unbind(oldName);
		unbind(newName);
		bind(newName, oldValue);
	}

	/** {@inheritDoc} */
	public void rename(String oldName, String newName) throws NamingException {
		rename(nameParser.parse(oldName), nameParser.parse(newName));
	}

	protected void setNameInNamespace(Name name) throws NamingException {
		if (nameInNamespace != null && !nameInNamespace.isEmpty()) {
			throw new NamingException("Name already set.");
		}
		nameInNamespace = name;
	}

	/** {@inheritDoc} */
	public void unbind(Name name) throws NamingException {

		if (name.isEmpty()) {
			throw new InvalidNameException("Cannot unbind to empty name");
		}

		if (name.size() == 1) {
			if (table.containsKey(name)) {
				table.remove(name);
			}
			return;
		}

		Object targetContext = lookup(name.getPrefix(name.size() - 1));
		if (targetContext == null || !(targetContext instanceof Context)) {
			throw new NamingException("Cannot unbind object. Target context does not exist.");
		}

		((Context) targetContext).unbind(name.getSuffix(name.size() - 1));
	}

	/** {@inheritDoc} */
	public void unbind(String name) throws NamingException {
		unbind(nameParser.parse(name));
	}
}
