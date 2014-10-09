/*
 * Copyright 2014 mango.jfaster.org
 *
 * The Mango Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package org.jfaster.mango.operator;

import org.jfaster.mango.util.reflect.Parameter;
import org.jfaster.mango.util.reflect.ParameterDescriptor;

import java.util.ArrayList;
import java.util.List;

/**
 * @author ash
 */
public class InvocationInterceptorChain {

    private InterceptorChain interceptorChain;

    private List<ParameterDescriptor> parameterDescriptors;

    public InvocationInterceptorChain(InterceptorChain interceptorChain,
                                      List<ParameterDescriptor> parameterDescriptors) {
        this.interceptorChain = interceptorChain;
        this.parameterDescriptors = parameterDescriptors;
    }

    public void intercept(PreparedSql preparedSql, InvocationContext context) {
        if (interceptorChain.getInterceptors() != null) {
            List<Object> parameterValues = context.getParameterValues();
            List<Parameter> parameters = new ArrayList<Parameter>(parameterValues.size());
            for (int i = 0; i < parameterValues.size(); i++) {
                ParameterDescriptor pd = parameterDescriptors.get(i);
                parameters.add(new Parameter(pd, parameterValues.get(i)));
            }
            interceptorChain.intercept(preparedSql, parameters);
        }
    }

}
