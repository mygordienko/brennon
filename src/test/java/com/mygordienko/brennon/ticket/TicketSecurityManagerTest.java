package com.mygordienko.brennon.ticket;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.jdbc.core.mapping.AggregateReference;

import com.mygordienko.brennon.core.auth.BrennonUserId;
import com.mygordienko.brennon.core.auth.BrennonUserProfile;

public class TicketSecurityManagerTest {
    @Test
    @DisplayName("Author has access to their tickets")
    void givenExistingTIcketWhenAccessedByAuthorThenAccessGranted() {
        
        var userId = BrennonUserId.of("1");
        var userProfile = Mockito.mock(BrennonUserProfile.class);
        Mockito.when(userProfile.getId()).thenReturn(userId);
        
        var ticket = Mockito.mock(Ticket.class);
        var authorRef = AggregateReference.<BrennonUserProfile, BrennonUserId>to(userProfile.getId());
        Mockito.when(ticket.getAuthor()).thenReturn(authorRef);

        var ticketSecurity =  new TicketSecurityManager();

        Assertions.assertThat(ticketSecurity.userHasAccess(userProfile, ticket)).isTrue();
    }
}
