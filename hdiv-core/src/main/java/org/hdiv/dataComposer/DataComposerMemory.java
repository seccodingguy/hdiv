/**
 * Copyright 2005-2013 hdiv.org
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
package org.hdiv.dataComposer;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hdiv.config.HDIVConfig;
import org.hdiv.state.IPage;
import org.hdiv.state.IParameter;
import org.hdiv.state.IState;
import org.hdiv.state.Parameter;
import org.hdiv.state.State;
import org.hdiv.util.Constants;
import org.springframework.web.util.HtmlUtils;

/**
 * <p>
 * It generates the states of each page by storing them in the user session. To be able to associate the request state
 * with the state stored in session, an extra parameter is added to each request, containing the state identifier which
 * makes possible to get the state of the user session.
 * </p>
 * <p>
 * Non editable values are hidden to the client, guaranteeing <b>confidentiality</b>
 * </p>
 * 
 * @see org.hdiv.dataComposer.AbstractDataComposer
 * @see org.hdiv.dataComposer.IDataComposer
 * @author Roberto Velasco
 */
public class DataComposerMemory extends AbstractDataComposer {

	/**
	 * Commons Logging instance.
	 */
	private static Log log = LogFactory.getLog(DataComposerMemory.class);

	/**
	 * Represents the identifier of each possible state stored in the page <code>page</code>.
	 */
	protected int requestCounter = 0;

	/**
	 * HDIV configuration object.
	 */
	protected HDIVConfig hdivConfig;

	/**
	 * It generates a new encoded value for the parameter <code>parameter</code> and the value <code>value</code> passed
	 * as parameters. The returned value guarantees the confidentiality in the encoded and memory strategies if
	 * confidentiality indicator <code>confidentiality</code> is true.
	 * 
	 * @param parameter
	 *            HTTP parameter name
	 * @param value
	 *            value generated by server
	 * @param editable
	 *            parameter type: editable(textbox, password,etc.) or non editable (hidden, select, radio, ...)
	 * @return Codified value to send to the client
	 */
	public String compose(String parameter, String value, boolean editable) {
		return this.compose(parameter, value, editable, false);
	}

	/**
	 * It generates a new encoded value for the parameter <code>parameter</code> and the value <code>value</code> passed
	 * as parameters. The returned value guarantees the confidentiality in the encoded and memory strategies if
	 * confidentiality indicator <code>confidentiality</code> is true.
	 * 
	 * @param action
	 *            target action
	 * @param parameter
	 *            HTTP parameter name
	 * @param value
	 *            value generated by server
	 * @param editable
	 *            parameter type: editable(textbox, password,etc.) or non editable (hidden, select, radio, ...)
	 * @return Codified value to send to the client
	 */
	public String compose(String action, String parameter, String value, boolean editable) {

		return this.compose(action, parameter, value, editable, false, Constants.ENCODING_UTF_8);
	}

	/**
	 * Adds a new IParameter object, generated from the values passed as parameters, to the current state
	 * <code>state</code>. If confidentiality is activated it generates a new encoded value that will be returned by the
	 * server for the parameter <code>parameter</code> in the encoded and memory strategies.
	 * 
	 * @param parameter
	 *            HTTP parameter
	 * @param value
	 *            value generated by server
	 * @param editable
	 *            Parameter type: editable(textbox, password,etc.) or non editable (hidden, select, radio, ...)
	 * @param isActionParam
	 *            parameter added in action attribute
	 * @return Codified value to send to the client
	 */
	public String compose(String parameter, String value, boolean editable, boolean isActionParam) {

		return this.compose(parameter, value, editable, isActionParam, Constants.ENCODING_UTF_8);
	}

	/**
	 * It generates a new encoded value for the parameter <code>parameter</code> and the value <code>value</code> passed
	 * as parameters. The returned value guarantees the confidentiality in the encoded and memory strategies if
	 * confidentiality indicator <code>confidentiality</code> is true.
	 * 
	 * @param parameter
	 *            HTTP parameter name
	 * @param value
	 *            value generated by server
	 * @param editable
	 *            parameter type: editable(textbox, password,etc.) or non editable (hidden, select, radio, ...)
	 * @param editableName
	 *            editable name (text or textarea)
	 * @return Codified value to send to the client
	 * @since HDIV 1.1
	 */
	public String compose(String parameter, String value, boolean editable, String editableName) {

		return this.compose(parameter, value, editable, editableName, false, null, Constants.ENCODING_UTF_8);
	}

	/**
	 * It generates a new encoded value for the parameter <code>parameter</code> and the value <code>value</code> passed
	 * as parameters. The returned value guarantees the confidentiality in the encoded and memory strategies if
	 * confidentiality indicator <code>confidentiality</code> is true.
	 * 
	 * @param action
	 *            target action
	 * @param parameter
	 *            parameter name
	 * @param value
	 *            value generated by server
	 * @param editable
	 *            parameter type: editable(textbox, password,etc.) or non editable (hidden, select,...)
	 * @param isActionParam
	 *            parameter added in action attribute
	 * @param charEncoding
	 *            character encoding
	 * @return Codified value to send to the client
	 */
	public String compose(String action, String parameter, String value, boolean editable, boolean isActionParam,
			String charEncoding) {

		// Get actual IState
		IState state = this.getStatesStack().peek();
		if (state.getAction() != null && state.getAction().trim().length() == 0) {
			state.setAction(action);
		}
		return this.compose(parameter, value, editable, isActionParam, charEncoding);
	}

	/**
	 * Adds a new IParameter object, generated from the values passed as parameters, to the current state
	 * <code>state</code>. If confidentiality is activated it generates a new encoded value that will be returned by the
	 * server for the parameter <code>parameter</code> in the encoded and memory strategies.
	 * 
	 * @param parameter
	 *            HTTP parameter
	 * @param value
	 *            value generated by server
	 * @param editable
	 *            Parameter type: editable(textbox, password,etc.) or non editable (hidden, select, radio, ...)
	 * @param isActionParam
	 *            parameter added in action attribute
	 * @param charEncoding
	 *            character encoding
	 * @return Codified value to send to the client
	 */
	public String compose(String parameter, String value, boolean editable, boolean isActionParam, String charEncoding) {

		return this.compose(parameter, value, editable, null, isActionParam, null, charEncoding);
	}

	/**
	 * Adds a new IParameter object, generated from the values passed as parameters, to the current state
	 * <code>state</code>. If confidentiality is activated it generates a new encoded value that will be returned by the
	 * server for the parameter <code>parameter</code> in the encoded and memory strategies.
	 * 
	 * @param parameter
	 *            HTTP parameter
	 * @param value
	 *            value generated by server
	 * @param editable
	 *            Parameter type: editable(textbox, password,etc.) or non editable (hidden, select, radio, ...)
	 * @param editableName
	 *            editable name (text or textarea)
	 * @param isActionParam
	 *            parameter added in action attribute
	 * @param method
	 *            http method, GET or POST
	 * @return Codified value to send to the client
	 * @since HDIV 2.1.5
	 */
	public String compose(String parameter, String value, boolean editable, String editableName, boolean isActionParam,
			String method) {
		return this.compose(parameter, value, editable, editableName, isActionParam, method, Constants.ENCODING_UTF_8);
	}

	/**
	 * Adds a new IParameter object, generated from the values passed as parameters, to the current state
	 * <code>state</code>. If confidentiality is activated it generates a new encoded value that will be returned by the
	 * server for the parameter <code>parameter</code> in the encoded and memory strategies.
	 * <p>
	 * Custom method for form field.
	 * 
	 * @param parameter
	 *            HTTP parameter
	 * @param value
	 *            value generated by server
	 * @param editable
	 *            Parameter type: editable(textbox, password,etc.) or non editable (hidden, select, radio, ...)
	 * @param editableName
	 *            editable name (text or textarea)
	 * @return Codified value to send to the client
	 * @since HDIV 2.1.5
	 */
	public String composeFormField(String parameter, String value, boolean editable, String editableName) {

		return this.compose(parameter, value, editable, editableName, false, "POST", Constants.ENCODING_UTF_8);
	}

	/**
	 * Adds a new IParameter object, generated from the values passed as parameters, to the current state
	 * <code>state</code>. If confidentiality is activated it generates a new encoded value that will be returned by the
	 * server for the parameter <code>parameter</code> in the encoded and memory strategies.
	 * 
	 * @param parameterName
	 *            HTTP parameter
	 * @param value
	 *            value generated by server
	 * @param editable
	 *            Parameter type: editable(textbox, password,etc.) or non editable (hidden, select, radio, ...)
	 * @param editableName
	 *            editable name (text or textarea)
	 * @param isActionParam
	 *            parameter added in action attribute
	 * @param method
	 *            http method, GET or POST
	 * @param charEncoding
	 *            character encoding
	 * @return Codified value to send to the client
	 * @since HDIV 2.1.5
	 */
	public String compose(String parameterName, String value, boolean editable, String editableName,
			boolean isActionParam, String method, String charEncoding) {

		if (!this.isRequestStarted()) {
			// If request not started, do nothing
			return value;
		}

		if (method == null || method.length() == 0) {
			// Default method is GET
			method = "GET";
		}

		IParameter parameter = this.composeParameter(parameterName, value, editable, editableName, isActionParam,
				charEncoding);

		if (this.isConfidentialParam(parameterName, method)) {
			return parameter.getConfidentialValue();
		} else {
			return value;
		}

	}

	/**
	 * Returns true if the parameter requires confidentiality. False otherwise.
	 * 
	 * @param parameterName
	 *            the name of the parameter
	 * @param method
	 *            request HTTP method
	 * @return boolean result
	 * @since HDIV 2.1.6
	 */
	protected boolean isConfidentialParam(String parameterName, String method) {

		if (!this.hdivConfig.getConfidentiality()) {
			return false;
		}

		if (this.hdivConfig.isStartParameter(parameterName)) {
			return false;
		}

		if (this.isUserDefinedNonValidationParameter(parameterName)) {
			return false;
		}

		if (this.hdivConfig.isParameterWithoutConfidentiality(parameterName)) {
			return false;
		}

		return true;
	}

	/**
	 * Checks if the parameter <code>parameter</code> is defined by the user as a no required validation parameter for
	 * the action <code>this.target</code>.
	 * 
	 * @param parameter
	 *            parameter name
	 * @return True If it is parameter that needs no validation. False otherwise.
	 * @since HDIV 2.0.6
	 */
	protected boolean isUserDefinedNonValidationParameter(String parameter) {

		// Get actual IState
		IState state = this.getStatesStack().peek();
		String action = state.getAction();

		if (this.hdivConfig.isParameterWithoutValidation(action, parameter)) {

			if (log.isDebugEnabled()) {
				log.debug("parameter " + parameter + " doesn't need validation. It is user defined parameter.");
			}
			return true;
		}
		return false;
	}

	/**
	 * Adds a new IParameter object, generated from the values passed as parameters, to the current state
	 * <code>state</code>.
	 * 
	 * @param parameterName
	 *            HTTP parameter
	 * @param value
	 *            value generated by server
	 * @param editable
	 *            Parameter type: editable(textbox, password,etc.) or non editable (hidden, select, radio, ...)
	 * @param editableDataType
	 *            editable parameter name (text or textarea)
	 * @param isActionParam
	 *            parameter added in action attribute
	 * @param charEncoding
	 *            character encoding
	 * @return Codified value to send to the client
	 * @since HDIV 1.1
	 */
	protected IParameter composeParameter(String parameterName, String value, boolean editable,
			String editableDataType, boolean isActionParam, String charEncoding) {

		// we decoded value before store it in state.
		String decodedValue = this.getDecodedValue(value, charEncoding);
		// Get actual IState
		IState state = this.getStatesStack().peek();

		IParameter parameter = state.getParameter(parameterName);
		if (parameter != null) {
			parameter.addValue(decodedValue);
		} else {
			// create a new parameter and add to the request
			parameter = createParameter(parameterName, decodedValue, editable, editableDataType, isActionParam,
					charEncoding);
			state.addParameter(parameter);
		}

		return parameter;
	}

	/**
	 * Instantiates the parameter
	 * 
	 * @param parameterName
	 *            name of the parameter
	 * @param decodedValue
	 *            the decoded value of the parameter
	 * @param editable
	 *            Parameter type: editable(textbox, password,etc.) or non editable (hidden, select, radio, ...)
	 * @param editableDataType
	 *            editable parameter name (text or textarea)
	 * @param isActionParam
	 *            parameter added in action attribute
	 * @param charEncoding
	 *            character encoding
	 * @return New IParameter object
	 */
	protected IParameter createParameter(String parameterName, String decodedValue, boolean editable,
			String editableDataType, boolean isActionParam, String charEncoding) {
		return new Parameter(parameterName, decodedValue, editable, editableDataType, isActionParam);
	}

	/**
	 * Creates a new parameter called <code>newParameter</code> and adds all the values of <code>oldParameter</code>
	 * stored in the state to it.
	 * 
	 * @param oldParameter
	 *            name of the parameter stored in the state
	 * @param newParameter
	 *            name of the new parameter
	 */
	public void mergeParameters(String oldParameter, String newParameter) {

		// Get actual IState
		IState state = this.getStatesStack().peek();
		IParameter storedParameter = state.getParameter(oldParameter);

		if (storedParameter.getValues().size() > 0) {

			IParameter parameter = this.composeParameter(newParameter, storedParameter.getValuePosition(0), false, "",
					false, Constants.ENCODING_UTF_8);

			String currentValue = null;
			// We check the parameters since the second position because the first
			// value has been used to create the parameter
			for (int i = 1; i < storedParameter.getValues().size(); i++) {

				currentValue = storedParameter.getValuePosition(i);
				parameter.addValue(currentValue);
			}
		}
	}

	/**
	 * <p>
	 * Decoded <code>value</code> using input <code>charEncoding</code>.
	 * </p>
	 * <p>
	 * Removes Html Entity elements too. Like that:
	 * </p>
	 * <blockquote> &amp;#<i>Entity</i>; - <i>(Example: &amp;amp;) case sensitive</i> &amp;#<i>Decimal</i>; -
	 * <i>(Example: &amp;#68;)</i><br>
	 * &amp;#x<i>Hex</i>; - <i>(Example: &amp;#xE5;) case insensitive</i><br>
	 * </blockquote>
	 * <p>
	 * Based on {@link HtmlUtils.htmlUnescape}.
	 * </p>
	 * 
	 * @param value
	 *            value to decode
	 * @param charEncoding
	 *            character encoding
	 * @return value decoded
	 */
	private String getDecodedValue(String value, String charEncoding) {

		String decodedValue = null;
		try {
			decodedValue = URLDecoder.decode(value, charEncoding);
		} catch (UnsupportedEncodingException e) {
			decodedValue = value;
		}

		if (decodedValue == null) {
			return "";
		}

		// Remove escaped Html elements
		if (decodedValue.contains("&")) {
			// Can contain escaped characters
			decodedValue = HtmlUtils.htmlUnescape(decodedValue);
		}

		return (decodedValue == null) ? "" : decodedValue;
	}

	/**
	 * It is called by each request or form existing in the page returned by the server. It creates a new state to store
	 * all the parameters and values of the request or form.
	 * 
	 * @return state id for this request
	 */
	public String beginRequest() {

		return this.beginRequest("");
	}

	/**
	 * It is called in the pre-processing stage of each request or form existing in the page returned by the server, as
	 * long as the destiny of the request is an action. It creates a new state to store all the parameters and values of
	 * the request or form.
	 * 
	 * @param action
	 *            action name
	 * @return state id for this request
	 * 
	 * @see org.hdiv.dataComposer.DataComposerMemory#beginRequest()
	 */
	public String beginRequest(String action) {

		try {
			action = URLDecoder.decode(action, Constants.ENCODING_UTF_8);
		} catch (UnsupportedEncodingException e) {
		}

		// Create new IState
		IState state = new State(this.requestCounter);
		state.setAction(action);

		return this.beginRequest(state);
	}

	public String beginRequest(IState state) {

		this.getStatesStack().push(state);

		this.requestCounter = state.getId() + 1;

		String id = this.getPage().getName() + DASH + state.getId() + DASH + this.getHdivStateSuffix();
		return id;
	}

	/**
	 * It is called in the pre-processing stage of each request or form existing in the page returned by the server. It
	 * adds the state of the treated request or form to the page <code>page</code> and returns and identifier composed
	 * by the page identifier and the state identifier.
	 * 
	 * @return Identifier composed by the page identifier and the state identifier.
	 */
	public String endRequest() {

		IState state = this.getStatesStack().pop();

		IPage page = this.getPage();
		state.setPageId(page.getName());
		page.addState(state);

		// Save Page in session if this is the first state to add
		boolean firstState = page.getStatesCount() == 1;
		if (firstState) {

			super.session.addPage(page.getName(), page);
		}

		String id = this.getPage().getName() + DASH + state.getId() + DASH + this.getHdivStateSuffix();
		return id;
	}

	/**
	 * Obtains the suffix to add to the _HDIV_STATE_ parameter in the memory version.
	 * 
	 * @return Returns suffix added to the _HDIV_STATE_ parameter in the memory version.
	 * @since HDIV 1.1
	 */
	protected String getHdivStateSuffix() {
		return this.getPage().getRandomToken();
	}

	/**
	 * It is called in the pre-processing stage of each user request assigning a new page identifier to the page.
	 */
	public void startPage() {

		this.initPage();
	}

	/**
	 * It is called in the pre-processing stage of each user request. Create a new {@link IPage} based on an existing
	 * page.
	 * 
	 * @param existingPage
	 *            other IPage
	 */
	public void startPage(IPage existingPage) {

		this.setPage(existingPage);
	}

	/**
	 * This method is called in the pre-processing stage of each user request to add an IPage object, which represents
	 * the page to show by the server, with all its states to the user session.
	 */
	public void endPage() {

		if (this.isRequestStarted()) {
			// A request is started but not ended
			this.endRequest();
		}

		IPage page = this.getPage();
		if (page.getStatesCount() > 0) {
			// The page has states, update them in session
			super.session.addPage(page.getName(), page);
		} else {
			if (log.isDebugEnabled()) {
				log.debug("The page [" + page.getName() + "] has no states, is not stored in session");
			}
		}

	}

	/**
	 * @param hdivConfig
	 *            The HDIV configuration object to set.
	 */
	public void setHdivConfig(HDIVConfig hdivConfig) {
		this.hdivConfig = hdivConfig;
	}

	/**
	 * Adds the flow identifier to the page of type <code>IPage</code>.
	 * 
	 * @since HDIV 2.0.3
	 */
	public void addFlowId(String id) {
		super.getPage().setFlowId(id);
	}

}