package org.example.simple.config;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class RpcServiceConfig {

    private String version = "";

    private String group = "";

    private Object service;

    public String getRpcServiceName() {
        return this.getServiceName() + this.getGroup() + this.getVersion();
    }

    public String getServiceName() {
        /**
         * 获得service对象所实现的第一个接口
         * 例: service = new ArrayList<Integer>();
         * 输出：java.util.List                下标：0
         *      java.util.RandomAccess            1
         *      java.lang.Cloneable               2
         *      java.io.Serializable              3
         */
        return this.service.getClass().getInterfaces()[0].getCanonicalName();
    }
}
