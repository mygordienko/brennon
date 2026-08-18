package com.mygordienko.brennon.ticket;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.mygordienko.brennon.core.auth.BrennonUserId;
import com.mygordienko.brennon.core.auth.BrennonUserProfile;

public class TicketSecurityManagerTest {
    @Test
    @DisplayName("Author has access to their tickets")
    void givenExistingTIcketWhenAccessedByAuthorThenAccessGranted() {
        
        var userId = BrennonUserId.of("1");
        var userProfile = Mockito.mock(BrennonUserProfile.class);
        Mockito.when(userProfile.id()).thenReturn(userId);
        
        var ticket = Mockito.mock(Ticket.class);
        Mockito.when(ticket.getAuthor()).thenReturn(userProfile);

        var ticketSecurity =  new TicketSecurityManager();

        Assertions.assertThat(ticketSecurity.userHasAccess(userProfile, ticket)).isTrue();
    }
}
