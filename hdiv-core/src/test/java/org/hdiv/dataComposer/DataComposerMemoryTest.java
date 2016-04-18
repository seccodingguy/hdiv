/**
 * Copyright 2005-2015 hdiv.org
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

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.hdiv.AbstractHDIVTestCase;
import org.hdiv.context.RequestContext;
import org.hdiv.session.ISession;
import org.hdiv.state.IPage;
import org.hdiv.state.IParameter;
import org.hdiv.state.IState;
import org.hdiv.state.StateUtil;
import org.hdiv.util.Constants;
import org.hdiv.util.HDIVUtil;
import org.hdiv.util.Method;
import org.springframework.mock.web.MockHttpServletRequest;

/**
 * Unit tests for the {@link DataComposerMemory} class.
 * 
 * @author Gorka Vicente
 */
public class DataComposerMemoryTest extends AbstractHDIVTestCase {

	private DataComposerFactory dataComposerFactory;

	private StateUtil stateUtil;

	private ISession session;

	/*
	 * @see TestCase#setUp()
	 */
	@Override
	protected void onSetUp() throws Exception {

		dataComposerFactory = getApplicationContext().getBean(DataComposerFactory.class);
		stateUtil = getApplicationContext().getBean(StateUtil.class);
		session = getApplicationContext().getBean(ISession.class);
	}

	/**
	 * @see DataComposerMemory#compose(String, String, String, boolean)
	 */
	public void testComposeSimple() {

		final HttpServletRequest request = getMockRequest();
		final IDataComposer dataComposer = dataComposerFactory.newInstance(request);
		HDIVUtil.setDataComposer(dataComposer, request);
		assertTrue(dataComposer instanceof DataComposerMemory);

		dataComposer.startPage();
		dataComposer.beginRequest(Method.GET, "test.do");

		final boolean confidentiality = getConfig().getConfidentiality();

		// we add a multiple parameter that will be encoded as 0, 1, 2, ...
		String result = dataComposer.compose("test.do", "parameter1", "2", false);
		String value = (!confidentiality) ? "2" : "0";
		assertTrue(value.equals(result));

		result = dataComposer.compose("test.do", "parameter1", "2", false);
		value = (!confidentiality) ? "2" : "1";
		assertTrue(value.equals(result));

		result = dataComposer.compose("test.do", "parameter1", "2", false);
		assertTrue("2".equals(result));

		result = dataComposer.compose("test.do", "parameter2", "2", false);
		value = (!confidentiality) ? "2" : "0";
		assertTrue(value.equals(result));

		result = dataComposer.compose("test.do", "parameter2", "2", false);
		value = (!confidentiality) ? "2" : "1";
		assertTrue(value.equals(result));
	}

	public void testComposeAndRestore() {

		final HttpServletRequest request = getMockRequest();
		final RequestContext context = getRequestContext();
		final IDataComposer dataComposer = dataComposerFactory.newInstance(request);
		HDIVUtil.setDataComposer(dataComposer, request);

		dataComposer.startPage();
		dataComposer.beginRequest(Method.GET, "test.do");
		dataComposer.compose("parameter1", "2", false);
		final String stateId = dataComposer.endRequest();
		dataComposer.endPage();

		assertNotNull(stateId);

		final IState state = stateUtil.restoreState(context, stateId);

		assertEquals("test.do", state.getAction());
		final List<String> values = state.getParameter("parameter1").getValues();
		assertEquals(1, values.size());
		assertEquals("2", values.get(0));
	}

	public void testComposeAndRestoreUrl() {

		final HttpServletRequest request = getMockRequest();
		final RequestContext context = getRequestContext();
		final IDataComposer dataComposer = dataComposerFactory.newInstance(request);

		dataComposer.startPage();
		dataComposer.beginRequest(Method.GET, "test.do");
		final String params = "param1=val1&param2=val2";
		final String processedParams = dataComposer.composeParams(params, Method.GET, Constants.ENCODING_UTF_8);
		assertEquals("param1=0&param2=0", processedParams);
		final String stateId = dataComposer.endRequest();
		dataComposer.endPage();

		assertNotNull(stateId);

		final IState state = stateUtil.restoreState(context, stateId);

		assertEquals("test.do", state.getAction());
		final String stateParams = state.getParams();
		assertEquals(params, stateParams);
	}

	public void testComposeExistingState() {
		final HttpServletRequest request = getMockRequest();
		final RequestContext context = getRequestContext();

		IDataComposer dataComposer = dataComposerFactory.newInstance(request);
		HDIVUtil.setDataComposer(dataComposer, request);

		dataComposer.startPage();
		dataComposer.beginRequest(Method.POST, "test.do");
		dataComposer.compose("parameter1", "2", false);
		final String stateId = dataComposer.endRequest();
		dataComposer.endPage();

		assertNotNull(stateId);

		// New request
		final IState state = stateUtil.restoreState(context, stateId);
		final IPage page = session.getPage(context, state.getPageId());
		dataComposer = dataComposerFactory.newInstance(request);
		HDIVUtil.setDataComposer(dataComposer, request);

		dataComposer.startPage(page);
		dataComposer.beginRequest(state);
		dataComposer.compose("parameter1", "3", false);
		final String stateId2 = dataComposer.endRequest();
		dataComposer.endPage();

		assertEquals(stateId, stateId2);
		final IState state2 = stateUtil.restoreState(context, stateId2);
		assertEquals(state2.getParameter("parameter1").getConfidentialValue(), "1");
		assertTrue(state2.getParameter("parameter1").existValue("2"));
		assertTrue(state2.getParameter("parameter1").existValue("3"));
	}

	public void testInnerState() {

		final HttpServletRequest request = getMockRequest();
		final RequestContext context = getRequestContext();
		final IDataComposer dataComposer = dataComposerFactory.newInstance(request);
		HDIVUtil.setDataComposer(dataComposer, request);

		dataComposer.startPage();
		dataComposer.beginRequest(Method.POST, "test.do");
		dataComposer.compose("parameter1", "2", false);

		// Start inner state
		dataComposer.beginRequest(Method.GET, "testinner.do");
		dataComposer.compose("parameter1", "3", false);
		final String stateIdInner = dataComposer.endRequest();

		final String stateId = dataComposer.endRequest();
		dataComposer.endPage();

		assertNotNull(stateId);
		assertNotNull(stateIdInner);
		assertNotSame(stateId, stateIdInner);

		final IState state = stateUtil.restoreState(context, stateId);
		final IState stateInner = stateUtil.restoreState(context, stateIdInner);
		final String action = state.getAction();
		final String actionInner = stateInner.getAction();
		assertEquals("test.do", action);
		assertEquals("testinner.do", actionInner);
	}

	public void testEscapeHtml() {

		final HttpServletRequest request = getMockRequest();
		final RequestContext context = getRequestContext();
		final IDataComposer dataComposer = dataComposerFactory.newInstance(request);
		HDIVUtil.setDataComposer(dataComposer, request);

		dataComposer.startPage();
		dataComposer.beginRequest(Method.POST, "test.do");
		dataComposer.compose("parameter1", "è-test", false);// not escaped value
		dataComposer.compose("parameterEscaped", "&egrave;-test", false);// escaped value
		final String stateId = dataComposer.endRequest();
		dataComposer.endPage();

		assertNotNull(stateId);

		final IState state = stateUtil.restoreState(context, stateId);

		assertEquals("test.do", state.getAction());

		final IParameter param = state.getParameter("parameter1");
		final List<String> values = param.getValues();
		assertEquals(1, values.size());
		assertEquals("è-test", values.get(0));// escaped value is the same

		final IParameter param2 = state.getParameter("parameterEscaped");
		final List<String> values2 = param2.getValues();
		assertEquals(1, values2.size());
		// State stored value is not escaped value, it is the unescaped value
		assertEquals("è-test", values2.get(0));
	}

	public void testEditableNullValue() {

		final HttpServletRequest request = getMockRequest();
		final RequestContext context = getRequestContext();
		final IDataComposer dataComposer = dataComposerFactory.newInstance(request);
		HDIVUtil.setDataComposer(dataComposer, request);

		dataComposer.startPage();
		dataComposer.beginRequest(Method.POST, "test.do");
		dataComposer.compose("parameter1", "test", true);
		final String stateId = dataComposer.endRequest();
		dataComposer.endPage();

		assertNotNull(stateId);

		final IState state = stateUtil.restoreState(context, stateId);

		assertEquals("test.do", state.getAction());

		final IParameter param = state.getParameter("parameter1");
		final List<String> values = param.getValues();
		assertEquals(0, values.size());
	}

	public void testAjax() {

		final MockHttpServletRequest request = getMockRequest();
		final RequestContext context = getRequestContext();
		final IDataComposer dataComposer = dataComposerFactory.newInstance(request);
		HDIVUtil.setDataComposer(dataComposer, request);

		dataComposer.startPage();
		dataComposer.beginRequest(Method.POST, "test.do");
		dataComposer.compose("parameter1", "1", false);
		final String stateId = dataComposer.endRequest();
		dataComposer.endPage();

		assertNotNull(stateId);

		// Ajax request to modify state

		request.addParameter("_MODIFY_HDIV_STATE_", stateId);
		final IDataComposer dataComposer2 = dataComposerFactory.newInstance(request);
		HDIVUtil.setDataComposer(dataComposer2, request);

		// Add new parameter
		dataComposer2.compose("parameter2", "2", false);
		final String stateId2 = dataComposer2.endRequest();
		dataComposer2.endPage();

		assertEquals(stateId, stateId2);

		// Restore state
		final IState state = stateUtil.restoreState(context, stateId);

		// State contains both parameters
		IParameter param = state.getParameter("parameter1");
		String val = param.getValues().get(0);
		assertEquals("1", val);

		param = state.getParameter("parameter2");
		val = param.getValues().get(0);
		assertEquals("2", val);
	}

	public void testAjaxWithHeaderEnabledAjaxSupport() {
		getConfig().setReuseExistingPageInAjaxRequest(true);

		final HttpServletRequest request = getMockRequest();
		final RequestContext context = getRequestContext();
		final IDataComposer dataComposer = dataComposerFactory.newInstance(request);
		HDIVUtil.setDataComposer(dataComposer, request);

		dataComposer.startPage();
		dataComposer.beginRequest(Method.GET, "test.do");
		dataComposer.compose("parameter1", "1", false);
		final String stateId = dataComposer.endRequest();
		dataComposer.endPage();

		assertNotNull(stateId);

		// Ajax
		final MockHttpServletRequest ajaxRequest = getMockRequest();
		ajaxRequest.addHeader("x-requested-with", "XMLHttpRequest");
		ajaxRequest.addParameter("_HDIV_STATE_", stateId);
		final IDataComposer ajaxDataComposer = dataComposerFactory.newInstance(request);
		HDIVUtil.setDataComposer(ajaxDataComposer, ajaxRequest);

		// Ajax request to add states
		ajaxDataComposer.beginRequest(Method.GET, "/test/1");
		final String ajaxStateId = ajaxDataComposer.endRequest();

		// Restore states
		final IState state = stateUtil.restoreState(context, stateId);
		final IState ajaxState = stateUtil.restoreState(context, ajaxStateId);

		assertEquals(state.getPageId(), ajaxState.getPageId());
		assertEquals(state.getId() + 1, ajaxState.getId());
	}

	public void testAjaxWithHeaderDisabledAjaxSupport() {
		final HttpServletRequest request = getMockRequest();
		final IDataComposer dataComposer = dataComposerFactory.newInstance(request);
		HDIVUtil.setDataComposer(dataComposer, request);

		dataComposer.startPage();
		dataComposer.beginRequest(Method.GET, "test.do");
		dataComposer.compose("parameter1", "1", false);
		final String stateId = dataComposer.endRequest();
		dataComposer.endPage();

		assertNotNull(stateId);

		// Ajax
		final MockHttpServletRequest ajaxRequest = getMockRequest();
		final RequestContext context = getRequestContext();
		ajaxRequest.addHeader("x-requested-with", "XMLHttpRequest");
		ajaxRequest.addParameter("_HDIV_STATE_", stateId);
		final IDataComposer ajaxDataComposer = dataComposerFactory.newInstance(request);
		HDIVUtil.setDataComposer(ajaxDataComposer, ajaxRequest);

		// Ajax request to add states
		ajaxDataComposer.beginRequest(Method.GET, "/test/1");
		final String ajaxStateId = ajaxDataComposer.endRequest();

		// Restore states
		final int pageId = stateUtil.restoreState(context, stateId).getPageId();
		final int ajaxPageId = stateUtil.restoreState(context, ajaxStateId).getPageId();

		assertEquals(pageId + 1, ajaxPageId);
	}

	public void testSaveStateInCreation() {

		// Test the validation of a state before processing all page

		final MockHttpServletRequest request = getMockRequest();
		final RequestContext context = getRequestContext();
		final IDataComposer dataComposer = dataComposerFactory.newInstance(request);
		HDIVUtil.setDataComposer(dataComposer, request);

		dataComposer.startPage();

		dataComposer.beginRequest(Method.POST, "test.do");
		final String result = dataComposer.compose("test.do", "parameter1", "2", false);
		assertEquals("0", result);
		final String stateId = dataComposer.endRequest();

		final IState state = stateUtil.restoreState(context, stateId);
		assertNotNull(state);
		assertEquals("test.do", state.getAction());

		dataComposer.endPage();
	}

	public void testEncodeFormAction() {

		// No encoded url
		final MockHttpServletRequest request = getMockRequest();
		final RequestContext context = getRequestContext();
		IDataComposer dataComposer = dataComposerFactory.newInstance(request);
		HDIVUtil.setDataComposer(dataComposer, request);

		dataComposer.startPage();
		dataComposer.beginRequest(Method.POST, "test test.do");
		String stateId = dataComposer.endRequest();
		dataComposer.endPage();

		assertNotNull(stateId);

		IState state = stateUtil.restoreState(context, stateId);

		assertEquals("test test.do", state.getAction());

		// Encoded action url
		dataComposer = dataComposerFactory.newInstance(request);
		HDIVUtil.setDataComposer(dataComposer, request);

		dataComposer.startPage();
		dataComposer.beginRequest(Method.POST, "test%20test.do");
		stateId = dataComposer.endRequest();
		dataComposer.endPage();

		assertNotNull(stateId);

		state = stateUtil.restoreState(context, stateId);

		// State action value is decoded because we store decoded values only
		assertEquals("test test.do", state.getAction());
	}
}