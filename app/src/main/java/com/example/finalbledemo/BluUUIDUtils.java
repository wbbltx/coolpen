package com.example.finalbledemo;

import android.os.ParcelUuid;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.UUID;

/**
 * 蓝牙UUID和蓝牙指令
 */

public class BluUUIDUtils {
    public enum BtSmartUuid {
        UUID_SERVICE("0000fee9-0000-1000-8000-00805f9b34fb"),//service  UUID
        UUID_CHAR_READ("d44bc439-abfd-45a2-b575-925416129601"),//通知读取  UUID
        UUID_CHAR_WRITE("d44bc439-abfd-45a2-b575-925416129600");//蓝牙书写 UUID
        // Lookup table to allow reverse lookup.
        private static final HashMap<UUID, BtSmartUuid> lookup = new HashMap<UUID, BtSmartUuid>();

        // Populate the lookup table at load time
        static {
            for (BtSmartUuid s : EnumSet.allOf(BtSmartUuid.class))
                lookup.put(s.value, s);
        }

        private UUID value;

        BtSmartUuid(String value) {
            this.value = UUID.fromString(value);
        }

        /**
         * Reverse look up UUID -> BtSmartUuid
         *
         * @param uuid The UUID to get a enumerated value for.
         * @return Enumerated value of type BtSmartUuid.
         */
        public static BtSmartUuid get(UUID uuid) {
            return lookup.get(uuid);
        }

        public UUID getUuid() {
            return value;
        }

        public ParcelUuid getParcelable() {
            return new ParcelUuid(this.value);
        }
    }

    public enum BluInstruct {
        OBTAIN_KEY_STATE("0f0f570000"),//获取key状态命令
        NOT_KEY_WRITE("0f0f570206"),//无key时写入key
        HAVE_KEY_WRITE("0f0f570306"),//当有key是写入命令
        OPEN_STORAGE_CHANNEL("0f0f610101"),//打开存储通道
        OPEN_WRITE_CHANNEL("0f0f610100"),   //打开书写通道 该通道打开以后书写才会收到反馈
        QUERY_STORAGE_INFO("0f0f710000"),//查询是否有存储信息
        READ_STORAGE_INFO("0f0f81020000"),//读取存储信息
        EMPTY_STORAGE_DATA("0f0f81030000"),//清空存储数据
        OBTAIN_ELECTRICITY("0f0f571900")//获取电量指令
        ;
        private String instructValue;

        BluInstruct(String value) {
            this.instructValue = value;
        }

        public String getUuid() {
            return instructValue;
        }
    }

    public enum BluInstructReplyMsg {
        HAVE_KEY_STATE("0f0f57800101"),//有key状态返回
        NOT_KEY_STATE("0f0f57800100"),//无key状态返回
        NOT_KEY_WRITE_SUCCEED_STATE("0f0f57820100"),//无key时，写入key状态成功
        NOT_KEY_WRITE_FAILURE_STATE("0f0f57820101"),//无key时，写入key状态失败
        HAVE_KEY_WRITE_SUCCEED_STATE("0f0f57830100"),//有key时，写入key状态成功
        HAVE_KEY_WRITE_FAILURE_STATE("0f0f57830101"),//有key时，写入key状态失败
        NOT_STORAGE_INFO("0f0f710000"),//无存储信息
        HAVE_STORAGE_INFO("0f0f710001"),//有存储信息
        STORAGE_DATA_READ_END("0f0f81020000"),//存储数据读取完毕
        STORAGE_DATA_EMPTY_END("0f0f81030101"),//存储数据清空完毕
        ELECTRICITY_INFO("0f0f5799")//电量信息---前8位
        ;
        private String replyMsgValue;

        BluInstructReplyMsg(String value) {
            this.replyMsgValue = value;
        }

        public String getMsg() {
            return replyMsgValue;
        }
    }
}
