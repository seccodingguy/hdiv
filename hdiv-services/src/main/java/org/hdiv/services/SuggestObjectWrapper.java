/**
 * Copyright 2005-2016 hdiv.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.hdiv.services;

public class SuggestObjectWrapper<T> implements WrappedValue<T> {

	public static final String ID = Path.path(Path.on(SuggestObjectWrapper.class).getSvalue());

	public static final String TEXT = Path.path(Path.on(SuggestObjectWrapper.class).getText());

	private final String text;

	private final String svalue;

	private final T original;

	public SuggestObjectWrapper(final String text, final String id, final T original) {
		this.text = text;
		this.svalue = id;
		this.original = original;
	}

	public String getText() {
		return text;
	}

	public String getSvalue() {
		return svalue;
	}

	public T getValue() {
		return original;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (svalue == null ? 0 : svalue.hashCode());
		result = prime * result + (text == null ? 0 : text.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		SuggestObjectWrapper<?> other = (SuggestObjectWrapper<?>) obj;
		if (svalue == null) {
			if (other.svalue != null) {
				return false;
			}
		}
		else if (!svalue.equals(other.svalue)) {
			return false;
		}
		if (text == null) {
			if (other.text != null) {
				return false;
			}
		}
		else if (!text.equals(other.text)) {
			return false;
		}
		return true;
	}

}
