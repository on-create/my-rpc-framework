import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;

public class ZkServiceRegistryTest {

    @Test
    public void test() {
        InetSocketAddress inetSocketAddress = new InetSocketAddress("127.0.0.1", 9333);
        System.out.println(inetSocketAddress.toString());
    }
}
