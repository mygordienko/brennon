package com.mygordienko.brennon.ticket;

import java.util.Optional;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.jdbc.core.mapping.AggregateReference;

import com.mygordienko.brennon.core.auth.BrennonUserAccount;
import com.mygordienko.brennon.core.auth.BrennonUserId;
import com.mygordienko.brennon.core.auth.BrennonUserInfoApi;
import com.mygordienko.brennon.core.auth.BrennonUserLogin;
import com.mygordienko.brennon.core.auth.BrennonUserProfile;
import com.mygordienko.brennon.ticket.internal.Ticket;
import com.mygordienko.brennon.ticket.internal.TicketSecurityManager;

public class TicketSecurityManagerTest {
    @Test
    @DisplayName("Author has access to their tickets")
    void givenExistingTicketWhenAccessedByAuthorThenAccessGranted() {
        
        var userId = BrennonUserId.of("1");
        var userLogin = BrennonUserLogin.of("jack");
        var userProfile = Mockito.mock(BrennonUserProfile.class);
        Mockito.when(userProfile.getId()).thenReturn(userId);
        Mockito.when(userProfile.getLogin()).thenReturn(userLogin);
        
        var ticket = Mockito.mock(Ticket.class);
        var authorRef = AggregateReference.<BrennonUserProfile, BrennonUserId>to(userProfile.getId());
        Mockito.when(ticket.getAuthor()).thenReturn(authorRef);

        var userAccount = Mockito.mock(BrennonUserAccount.class);
        Mockito.when(userAccount.isSupport()).thenReturn(false);

        var userPort = Mockito.mock(BrennonUserInfoApi.class);
        Mockito.when(userPort.getUserAccount(userLogin)).thenReturn(Optional.of(userAccount));

        var ticketSecurity =  new TicketSecurityManager(userPort);

        Assertions.assertThat(ticketSecurity.userCanRead(userProfile, ticket)).isTrue();
    }

    @Test
    @DisplayName("Support has access to any tickets")
    void givenExistingTicketWhenAccessedBySupportThenAccessGranted() {
        var authorId = BrennonUserId.of("1");

        var userId = BrennonUserId.of("10");
        var userLogin = BrennonUserLogin.of("anna");
        var userProfile = Mockito.mock(BrennonUserProfile.class);
        Mockito.when(userProfile.getId()).thenReturn(userId);
        Mockito.when(userProfile.getLogin()).thenReturn(userLogin);
        
        var ticket = Mockito.mock(Ticket.class);
        var authorRef = AggregateReference.<BrennonUserProfile, BrennonUserId>to(authorId);
        Mockito.when(ticket.getAuthor()).thenReturn(authorRef);

        var userAccount = Mockito.mock(BrennonUserAccount.class);
        Mockito.when(userAccount.isSupport()).thenReturn(true);

        var userPort = Mockito.mock(BrennonUserInfoApi.class);
        Mockito.when(userPort.getUserAccount(userLogin)).thenReturn(Optional.of(userAccount));

        var ticketSecurity =  new TicketSecurityManager(userPort);

        Assertions.assertThat(ticketSecurity.userCanRead(userProfile, ticket)).isTrue();
    }
}
