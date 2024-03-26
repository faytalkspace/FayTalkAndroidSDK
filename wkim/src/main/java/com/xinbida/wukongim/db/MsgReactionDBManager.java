package com.xinbida.wukongim.db;

import static com.xinbida.wukongim.db.WKDBColumns.TABLE.messageReaction;

import android.content.ContentValues;
import android.database.Cursor;
import android.text.TextUtils;

import com.xinbida.wukongim.WKIM;
import com.xinbida.wukongim.WKIMApplication;
import com.xinbida.wukongim.entity.WKChannel;
import com.xinbida.wukongim.entity.WKChannelType;
import com.xinbida.wukongim.entity.WKMsgReaction;

import java.util.ArrayList;
import java.util.List;

/**
 * 4/16/21 1:46 PM
 * 消息回应
 */
class MsgReactionDBManager {
    private MsgReactionDBManager() {
    }

    private static class MessageReactionDBManagerBinder {
        final static MsgReactionDBManager manager = new MsgReactionDBManager();
    }

    public static MsgReactionDBManager getInstance() {
        return MessageReactionDBManagerBinder.manager;
    }

    public void insertReactions(List<WKMsgReaction> list) {
        if (list == null || list.size() == 0) return;
        for (int i = 0, size = list.size(); i < size; i++) {
            insertOrUpdate(list.get(i));
        }
    }

    public void update(WKMsgReaction reaction) {
        String[] update = new String[2];
        update[0] = reaction.messageID;
        update[1] = reaction.uid;
        ContentValues cv = new ContentValues();
        cv.put("is_deleted", reaction.isDeleted);
        cv.put("seq", reaction.seq);
        cv.put("emoji", reaction.emoji);
        WKIMApplication.getInstance().getDbHelper()
                .update(messageReaction, cv, "message_id=? and uid=?", update);

    }

    public synchronized void insertOrUpdate(WKMsgReaction reaction) {
//        boolean isExist = isExist(reaction.uid, reaction.messageID);
//        if (isExist) {
//            update(reaction);
//        } else {
//            insert(reaction);
//        }
        insert(reaction);
    }

    public void insert(WKMsgReaction reaction) {
        WKIMApplication.getInstance().getDbHelper()
                .insert(messageReaction, WKSqlContentValues.getContentValuesWithMsgReaction(reaction));
    }

    private boolean isExist(String uid, String messageID) {
        boolean isExist = false;
        try (Cursor cursor = WKIMApplication
                .getInstance()
                .getDbHelper().select(messageReaction, "message_id=? and uid=?", new String[]{messageID, uid}, null)) {
            if (cursor != null && cursor.moveToLast()) {
                isExist = true;
            }
        }
        return isExist;
    }

    public List<WKMsgReaction> queryWithMessageId(String messageID) {
        List<WKMsgReaction> list = new ArrayList<>();
        String sql = "select * from " + messageReaction + " where message_id=? and is_deleted=0 ORDER BY created_at desc";
        try (Cursor cursor = WKIMApplication.getInstance().getDbHelper().rawQuery(sql, new Object[]{messageID})) {
            if (cursor == null) {
                return list;
            }
            for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                WKMsgReaction reaction = serializeReaction(cursor);
                WKChannel channel = WKIM.getInstance().getChannelManager().getChannel(reaction.uid, WKChannelType.PERSONAL);
                if (channel != null) {
                    String showName = TextUtils.isEmpty(channel.channelRemark) ? channel.channelName : channel.channelRemark;
                    if (!TextUtils.isEmpty(showName))
                        reaction.name = showName;
                }
                list.add(reaction);
            }
        }
        return list;
    }

    public List<WKMsgReaction> queryWithMessageIds(List<String> messageIds) {
        List<WKMsgReaction> list = new ArrayList<>();
        List<String> channelIds = new ArrayList<>();
        try (Cursor cursor = WKIMApplication
                .getInstance()
                .getDbHelper().select(messageReaction, "message_id in (" + WKCursor.getPlaceholders(messageIds.size()) + ")", messageIds.toArray(new String[0]), "created_at desc")) {
            if (cursor == null) {
                return list;
            }
            for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                WKMsgReaction msgReaction = serializeReaction(cursor);
                channelIds.add(msgReaction.uid);
                list.add(msgReaction);
            }
        } catch (Exception ignored) {
        }
        //查询用户备注
        List<WKChannel> channelList = ChannelDBManager.getInstance().queryWithChannelIdsAndChannelType(channelIds, WKChannelType.PERSONAL);
        for (int i = 0, size = list.size(); i < size; i++) {
            for (int j = 0, len = channelList.size(); j < len; j++) {
                if (channelList.get(j).channelID.equals(list.get(i).uid)) {
                    list.get(i).name = TextUtils.isEmpty(channelList.get(j).channelRemark) ? channelList.get(j).channelName : channelList.get(j).channelRemark;
                }
            }
        }
        return list;
    }

    public WKMsgReaction queryWithMsgIdAndUIDAndText(String messageID, String uid, String emoji) {
        WKMsgReaction reaction = null;
        String sql = "select * from " + messageReaction
                + " where message_id=? and uid=? and emoji=?";
        try (Cursor cursor = WKIMApplication
                .getInstance()
                .getDbHelper().rawQuery(sql, new Object[]{messageID, uid, emoji})) {
            if (cursor == null) {
                return null;
            }
            if (cursor.moveToLast()) {
                reaction = serializeReaction(cursor);
            }
        }

        return reaction;
    }

    public WKMsgReaction queryWithMsgIdAndUID(String messageID, String uid) {
        WKMsgReaction reaction = null;
        String sql = "select * from " + messageReaction
                + " where message_id=? and uid=?";
        try (Cursor cursor = WKIMApplication
                .getInstance()
                .getDbHelper().rawQuery(sql, new Object[]{messageID, uid})) {
            if (cursor == null) {
                return null;
            }
            if (cursor.moveToLast()) {
                reaction = serializeReaction(cursor);
            }
        }

        return reaction;
    }

    public long queryMaxSeqWithChannel(String channelID, byte channelType) {
        int maxSeq = 0;
        String sql = "select max(seq) seq from " + messageReaction
                + " where channel_id=? and channel_type=? limit 0, 1";
        try {
            if (WKIMApplication.getInstance().getDbHelper() != null) {
                Cursor cursor = WKIMApplication
                        .getInstance()
                        .getDbHelper()
                        .rawQuery(sql, new Object[]{channelID, channelType});
                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        maxSeq = WKCursor.readInt(cursor, "seq");
                    }
                    cursor.close();
                }
            }
        } catch (Exception ignored) {
        }
        return maxSeq;
    }

    private WKMsgReaction serializeReaction(Cursor cursor) {
        WKMsgReaction reaction = new WKMsgReaction();
        reaction.channelID = WKCursor.readString(cursor, "channel_id");
        reaction.channelType = (byte) WKCursor.readInt(cursor, "channel_type");
        reaction.uid = WKCursor.readString(cursor, "uid");
        reaction.name = WKCursor.readString(cursor, "name");
        reaction.messageID = WKCursor.readString(cursor, "message_id");
        reaction.createdAt = WKCursor.readString(cursor, "created_at");
        reaction.seq = WKCursor.readLong(cursor, "seq");
        reaction.emoji = WKCursor.readString(cursor, "emoji");
        reaction.isDeleted = WKCursor.readInt(cursor, "is_deleted");
        return reaction;
    }
}
