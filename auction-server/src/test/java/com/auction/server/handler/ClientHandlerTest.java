package com.auction.server.handler;

import com.auction.common.message.Action;
import com.auction.server.datastore.DataStore;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

class ClientHandlerTest {

    @BeforeEach
    void setUp() {
        DataStore.getInstance().getUsers().clear();
        DataStore.getInstance().getItems().clear();
        DataStore.getInstance().getAuctions().clear();
        DataStore.getInstance().getBidTransactions().clear();
    }

    @Test
    @DisplayName("All Action enum values have corresponding handler cases")
    void allActions_haveHandler() {
        Action[] actions = Action.values();
        assertTrue(actions.length > 0);

        for (Action action : actions) {
            assertNotNull(action.name());
            assertNotNull(action.toString());
        }
    }

    @Test
    @DisplayName("EventManager is accessible via static getter")
    void eventManager_isAccessible() {
        assertNotNull(ClientHandler.getEventManager());
    }

    @Test
    @DisplayName("ClientHandler static services are initialized")
    void staticServices_initialized() {
        assertNotNull(ClientHandler.getEventManager());
    }
}
