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
package com.dattack.naming.standalone;

import java.text.MessageFormat;
import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NameAlreadyBoundException;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;

import com.dattack.naming.AbstractContext;

/**
 * A generic <tt>Context</tt> designed to be used by standalone applications without a web-container.
 * 
 * @author cvarela
 * @since 0.1
 */
public class StandaloneContext extends AbstractContext {

	public StandaloneContext(Hashtable<?, ?> env) {
		super(env);
	}

	private StandaloneContext(AbstractContext that) {
		super(that);
	}

	/**
	 * @see javax.naming.Context#createSubcontext(javax.naming.Name)
	 */
	public Context createSubcontext(Name name) throws NamingException {

		Hashtable<Name, Object> subContexts = getSubContexts();

		if (name.size() > 1) {
			if (subContexts.containsKey(name.getPrefix(1))) {
				Context subContext = (Context) subContexts.get(name.getPrefix(1));
				return subContext.createSubcontext(name.getSuffix(1));
			}
			throw new NameNotFoundException(MessageFormat.format("The subcontext ''{0}'' was not found.",
					name.getPrefix(1)));
		}

		if (lookup(name) != null) {
			throw new NameAlreadyBoundException();
		}

		Name contextName = getNameParser((Name) null).parse(getNameInNamespace());
		contextName.addAll(name);
		StandaloneContext subcontext = new StandaloneContext(this);
		subcontext.setNameInNamespace(contextName);
		bind(name, subcontext);
		return subcontext;
	}
}
