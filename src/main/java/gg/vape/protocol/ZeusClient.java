package gg.vape.protocol;

import gg.vape.friend.UserModel;
import gg.vape.protocol.PresenceState;
import gg.vape.friend.activity.ActivityItemStackPayload;
import gg.vape.protocol.packet.PingTargetData;
import gg.vape.protocol.packet.*;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import org.jetbrains.annotations.Nullable;

/** Offline compatibility client; packet operations are intentionally discarded. */
public final class ZeusClient {
    private final UserModel offlineUser = new UserModel(-1L, "Offline");

    public void c(UserModel ignoredUser, boolean ignoredAccept,
                  Consumer<GroupInviteStateResponsePacket> ignoredResponse,
                  Runnable ignoredFailure) {
    }

    public void o(PingTargetData ignoredTarget, Consumer<PingResponsePacket> ignoredResponse) {
    }

    public void R(long ignoredUserId, int ignoredX, int ignoredY, int ignoredZ) {
    }

    public void P(Map<Integer, ActivityItemStackPayload> ignoredItems) {
    }

    public void H(int ignoredSlot) {
    }

    public void p(long ignoredProfileId) {
    }

    public void v(PresenceState ignoredState) {
    }

    public void h(long[] ignoredUserIds) {
    }

    public void u(Consumer<GroupLeaveResponsePacket> ignoredResponse, Runnable ignoredFailure) {
    }

    public <R extends ZeusTrackedPacket<?>> void w(
            ZeusTrackedPacket<R> ignoredPacket, @Nullable Consumer<R> ignoredResponse,
            @Nullable Runnable ignoredFailure) {
    }

    public void J(UserModel ignoredUser, Consumer<GroupInviteResponsePacket> ignoredResponse,
                  Runnable ignoredFailure) {
    }

    public void i(UserModel ignoredUser, Consumer<FriendDeleteResponsePacket> ignoredResponse,
                  Runnable ignoredFailure) {
    }

    public void Y(GroupOption ignoredOption, Object ignoredValue) {
    }

    public void Z(int ignoredClicksPerSecond) {
    }

    public void y(Consumer<FriendsListResponsePacket> ignoredResponse) {
    }

    public void V(UserModel ignoredUser, Consumer<GroupUninviteResponsePacket> ignoredResponse,
                  Runnable ignoredFailure) {
    }

    public void a(@Nullable String ignoredServerAddress) {
    }

    public void N(Consumer<HandshakeResponsePacket> ignoredResponse) {
    }

    public void V(ZeusSerializablePacket ignoredPacket) {
    }

    public void U(String ignoredName, Consumer<UserDisplayNameResponsePacket> ignoredResponse,
                  Runnable ignoredFailure) {
    }

    public void C(UUID ignoredProfileId, String ignoredUsername) {
    }

    public void l(Consumer<GroupDeleteResponsePacket> ignoredResponse, Runnable ignoredFailure) {
    }

    public void M() {
    }

    public <R extends ZeusTrackedPacket<?>> void z(ZeusTrackedPacket<R> ignoredPacket,
                                                    Consumer<R> ignoredResponse) {
    }

    public void Y(long ignoredUserId, boolean ignoredAccepted,
                  Consumer<FriendRequestUpdateResponsePacket> ignoredResponse) {
    }

    public void p(UserModel ignoredUser, String ignoredMessage,
                  Consumer<ChatToFriendResponsePacket> ignoredResponse) {
    }

    public void c(UserModel ignoredUser, Consumer<ClientGroupLeaderKickResponsePacket> ignoredResponse,
                  Runnable ignoredFailure) {
    }

    public void w(Consumer<GroupCreateResponsePacket> ignoredResponse, Runnable ignoredFailure) {
    }

    public void L(String ignoredMessage, Consumer<GroupChatResponsePacket> ignoredResponse) {
    }

    public void s(UserModel ignoredUser, Consumer<ClientGroupLeaderPromoteResponsePacket> ignoredResponse,
                  Runnable ignoredFailure) {
    }

    public void Z(String ignoredUsername, Consumer<FriendRequestResponsePacket> ignoredResponse) {
    }

    public void N(int ignoredSlot, Map<Integer, ActivityItemStackPayload> ignoredItems) {
    }

    public void B() {
    }

    public void p() {
    }

    public void J(String ignoredAccessToken, Consumer<AuthenticationResponsePacket> ignoredResponse) {
    }

    public void Y() {
    }

    public UserModel i() {
        return offlineUser;
    }
}
