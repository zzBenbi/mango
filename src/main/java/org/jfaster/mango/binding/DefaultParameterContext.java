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

package org.jfaster.mango.binding;

import org.jfaster.mango.annotation.Rename;
import org.jfaster.mango.base.Strings;
import org.jfaster.mango.invoker.UnreachablePropertyException;
import org.jfaster.mango.reflect.ParameterDescriptor;

import javax.annotation.Nullable;
import java.lang.reflect.Type;
import java.util.*;

/**
 * @author ash
 */
public class DefaultParameterContext implements ParameterContext {

    /**
     * 位置到重命名后的变量名的映射
     */
    private final Map<Integer, String> positionToNameMap = new HashMap<Integer, String>();

    private final Map<String, Type> nameToTypeMap = new LinkedHashMap<String, Type>();

    private DefaultParameterContext(List<ParameterDescriptor> parameterDescriptors) {
        for (int i = 0; i < parameterDescriptors.size(); i++) {
            ParameterDescriptor pd = parameterDescriptors.get(i);
            Rename renameAnno = pd.getAnnotation(Rename.class);
            String parameterName = renameAnno != null ?
                    renameAnno.value() : // 优先使用注解中的名字
                    pd.getName();
            nameToTypeMap.put(parameterName, pd.getType());
            int position = pd.getPosition();
            positionToNameMap.put(position, parameterName);
        }
    }

    public static DefaultParameterContext create(List<ParameterDescriptor> parameterDescriptors) {
        return new DefaultParameterContext(parameterDescriptors);
    }

    @Override
    public String getParameterNameByPosition(int position) {
        String name = positionToNameMap.get(position);
        if (name == null) {
            throw new IllegalStateException("parameter name can not be found by position [" + position + "]");
        }
        return name;
    }

    @Override
    public BindingParameterInvoker getInvokerGroup(BindingParameter bindingParameter) {
        String parameterName = bindingParameter.getParameterName();
        String propertyPath = bindingParameter.getPropertyPath();
        Type type = nameToTypeMap.get(parameterName);
        if (type == null) {
            throw new BindingException("Parameter '" + parameterName + "' not found, " +
                    "available root parameters are " + nameToTypeMap.keySet());
        }
        try {
            BindingParameterInvoker invokerGroup = FunctionalBindingParameterInvoker.create(type, bindingParameter);
            return invokerGroup;
        } catch (UnreachablePropertyException e) {
            throw new BindingException("Parameter '" + Strings.getFullName(parameterName, propertyPath) +
                    "' can't be readable", e);
        }
    }

    @Override
    @Nullable
    public String tryExpandParameterName(BindingParameter bindingParameter) {
        if (!nameToTypeMap.containsKey(bindingParameter.getParameterName())) { // 根参数不存在才扩展
            BindingParameter newBindingParameter = bindingParameter.rightShift();
            List<String> parameterNames = new ArrayList<String>();
            for (Map.Entry<String, Type> entry : nameToTypeMap.entrySet()) {
                Type type = entry.getValue();
                try {
                    FunctionalBindingParameterInvoker.create(type, newBindingParameter);
                } catch (UnreachablePropertyException e) {
                    // 异常说明扩展失败
                    continue;
                }
                parameterNames.add(entry.getKey());
            }
            int num = parameterNames.size();
            if (num > 0) {
                if (num != 1) {
                    throw new BindingException("parameters " + parameterNames +
                            " has the same property '" + newBindingParameter.getPropertyPath() + "', so can't expand");
                }
                return parameterNames.get(0);
            }
        }
        return null;
    }


}
