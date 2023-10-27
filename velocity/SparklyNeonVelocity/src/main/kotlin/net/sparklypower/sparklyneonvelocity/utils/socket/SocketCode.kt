package net.sparklypower.sparklyneonvelocity.utils.socket

enum class SocketCode(val errorId: Int, val fancyName: String) {
    // 6xx - SUCCESS / INFO
    // 7xx - USER ERROR
    // 8xx - LORI ERROR
    UNAUTHORIZED(700, "Unauthorized"),
    FORBIDDEN(701, "Forbidden"),
    OFFLINE_PLAYER(702, "Offline Player"),
    UNKNOWN_PLAYER(703, "Unknown Player"),
    UNKNOWN_SERVER(704, "Unknown Server"),
    INVALID_NUMBER(710, "Invalid Number"),
    INSUFFICIENT_FUNDS(711, "Insufficient Funds"),
    ITEM_NOT_FOUND(712, "Item not found"),
    MEMBER_NOT_IN_GUILD(720, "Member is not in provided guild"),
    MEMBER_DISABLED_DIRECT_MESSAGES(721, "Member disabled direct messages"),
    CHANNEL_DOESNT_EXIST(722, "Channel doesn't exist"),
    CANT_TALK_IN_CHANNEL(723, "Can't talk in channel"),
    INVALID_MESSAGE(724, "Invalid message"),

    MISSING_PAYLOAD_HANDLER(810, "Missing Payload Handler"),
}