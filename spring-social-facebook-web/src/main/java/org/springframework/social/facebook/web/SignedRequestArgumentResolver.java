/*
 * Copyright 2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.social.facebook.web;

import java.util.Map;

import org.springframework.core.MethodParameter;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.support.WebArgumentResolver;
import org.springframework.web.context.request.NativeWebRequest;

/**
 * @author Craig Walls
 */
public class SignedRequestArgumentResolver implements WebArgumentResolver {

	private final SignedRequestDecoder signedRequestDecoder;

	/**
	 */
	public SignedRequestArgumentResolver(String appSecret) {
		this.signedRequestDecoder = new SignedRequestDecoder(appSecret);
	}
	
	public Object resolveArgument(MethodParameter parameter, NativeWebRequest request) throws Exception {
		SignedRequest annotation = parameter.getParameterAnnotation(SignedRequest.class);
		if (annotation == null) {
			return WebArgumentResolver.UNRESOLVED;
		}

		String signedRequest = request.getParameter("signed_request");
		if (signedRequest == null && annotation.required()) {
			throw new IllegalStateException("Required signed_request parameter is missing.");
		}
		
		if (signedRequest == null) {
			return null;
		}
		
		Class<?> parameterType = parameter.getParameterType();
		if (MultiValueMap.class.isAssignableFrom(parameterType)) {
			Map map = signedRequestDecoder.decodeSignedRequest(signedRequest, Map.class);
			LinkedMultiValueMap<String, Object> mvm = new LinkedMultiValueMap<String, Object>(map.size());
			mvm.setAll((Map<String, Object>) map);
			return mvm;
		}
		return signedRequestDecoder.decodeSignedRequest(signedRequest, parameterType);
	}
}