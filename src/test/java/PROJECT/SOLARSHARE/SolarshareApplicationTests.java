package PROJECT.SOLARSHARE;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class SolarshareApplicationTests {

    @Test
    void mainClassLoads() {
        assertDoesNotThrow(() -> {
            Class.forName("PROJECT.SOLARSHARE.SolarshareApplication");
        });
    }
}
