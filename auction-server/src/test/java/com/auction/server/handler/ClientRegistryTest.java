package com.auction.server.handler;

import com.auction.common.message.ServerPushMessage;
import org.junit.jupiter.api.*;

import java.io.*;

import static org.junit.jupiter.api.Assertions.*;

class ClientRegistryTest {

    private ClientRegistry registry;

    @BeforeEach
    void setUp() {
        registry = ClientRegistry.getInstance();
    }

    @Test
    @DisplayName("ClientRegistry is singleton")
    void registry_isSingleton() {
        assertSame(ClientRegistry.getInstance(), registry);
    }

    @Test
    @DisplayName("Register client increases count")
    void register_increasesCount() throws IOException {
        int before = registry.getClientCount();
        ObjectOutputStream out = createMockOutputStream();
        registry.register("test-client", out);
        assertEquals(before + 1, registry.getClientCount());
        registry.unregister("test-client");
    }

    @Test
    @DisplayName("Unregister client decreases count")
    void unregister_decreasesCount() throws IOException {
        ObjectOutputStream out = createMockOutputStream();
        registry.register("test-client-2", out);
        int afterRegister = registry.getClientCount();
        registry.unregister("test-client-2");
        assertEquals(afterRegister - 1, registry.getClientCount());
    }

    @Test
    @DisplayName("Register and unregister multiple clients")
    void multipleRegistrations() throws IOException {
        ObjectOutputStream out1 = createMockOutputStream();
        ObjectOutputStream out2 = createMockOutputStream();
        registry.register("c1", out1);
        registry.register("c2", out2);
        assertEquals(2, registry.getClientCount());
        registry.unregister("c1");
        assertEquals(1, registry.getClientCount());
        registry.unregister("c2");
        assertEquals(0, registry.getClientCount());
    }

    @Test
    @DisplayName("getClientCount returns non-negative")
    void getClientCount_nonNegative() {
        assertTrue(registry.getClientCount() >= 0);
    }

    @Test
    @DisplayName("Broadcast to empty registry does not throw")
    void broadcast_emptyRegistry() {
        ServerPushMessage msg = new ServerPushMessage(
                ServerPushMessage.PushType.NEW_BID, "test", null);
        assertDoesNotThrow(() -> registry.broadcast(msg));
    }

    private ObjectOutputStream createMockOutputStream() throws IOException {
        return new ObjectOutputStream(new ByteArrayOutputStream());
    }
}
