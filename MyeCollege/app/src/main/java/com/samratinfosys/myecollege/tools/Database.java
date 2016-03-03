package com.samratinfosys.myecollege.tools;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.samratinfosys.myecollege.json_classes.Account;
import com.samratinfosys.myecollege.json_classes.Chat;
import com.samratinfosys.myecollege.json_classes.ChatHead;
import com.samratinfosys.myecollege.json_classes.College;
import com.samratinfosys.myecollege.json_classes.Profile;
import com.samratinfosys.myecollege.json_classes.Timeline;
import com.samratinfosys.myecollege.utils.Helper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by iAmMegamohan on 18-04-2015.
 */
public class Database {

    public static final String DATABASE_FILE="MyeCollege.db";
    private Context context;
    private SQLiteDatabase sqLiteDatabase=null;

    public Database(Context context) {
        this.context=context;
        if(open()) {
            initDatabase(sqLiteDatabase);
        }
    }

    private void initDatabase(SQLiteDatabase db) {
        if(sqLiteDatabase==null) return;

        try {

            // college table
            db.execSQL("create table if not exists college(college_id integer primary key, college_short_name text, college_full_name text, college_city text, college_fax_number text, college_email_address text, college_telephone_number text, college_website text, college_code text, college_address text, college_type text, college_estd_year text, college_tution_fees text)");

            // account table
            db.execSQL("create table if not exists account(account_id integer primary key, account_type text, account_name text, account_user_id text, account_email_address text, account_mobile_number text, account_activation_status text, account_college_id integer)");

            // timeline table
            db.execSQL("create table if not exists timeline(timeline_id integer primary key, timeline_profile_id integer, timeline_college_id integer, timeline_type integer, timeline_timestamp timestamp, timeline_security_data text, timeline_data text)");

            // profile table
            db.execSQL("create table if not exists profile(profile_id integer primary key, profile_account_id integer, profile_first_name text, profile_last_name text, profile_mobile_number text, profile_email_address text, profile_timeline_status text, profile_dateofbirth date, profile_address text, profile_gender text, profile_semester int)");

            // chat table
            // db.execSQL("drop table chat");
            db.execSQL("create table if not exists chat(chat_id integer primary key, chat_propagation_type int, chat_from_id integer, chat_to_id integer, chat_timestamp timestamp, chat_security_data text, chat_message_type int, chat_message text, chat_status int, chat_from_name text, chat_to_name text)");

        } catch (Exception ex) {
            Helper.Log(ex,"initDatabase: "+DATABASE_FILE);
        }
    }

    public boolean open() {
        if(sqLiteDatabase==null)
            sqLiteDatabase=SQLiteDatabase.openOrCreateDatabase(Helper.getFile(context,DATABASE_FILE),null);
        return sqLiteDatabase.isOpen();
    }

    public void query(String query) {
        try {
            if(open()) {
                sqLiteDatabase.execSQL(query);
            }
        } catch (Exception ex) {
            Helper.Log(ex,"DatabaseQuery: "+query);
        }
    }

    /* ------------------------------------ CHAT ------------------------------------------------ */

    public long addChat(Chat chat) {
        if(open()) {
            ContentValues cv=new ContentValues();
            cv.put("chat_id",chat.id);
            cv.put("chat_propagation_type",Chat.fromEnum(chat.propagationType));
            cv.put("chat_from_id",chat.from_id);
            cv.put("chat_to_id",chat.to_id);
            cv.put("chat_timestamp",chat.timestamp);
            if(chat.security_data!=null) {
                cv.put("chat_security_data", chat.security_data);
            } else {
                cv.putNull("chat_security_data");
            }
            cv.put("chat_message_type",Chat.fromEnum(chat.message_type));
            cv.put("chat_message",chat.message.toString());
            cv.put("chat_status",Chat.fromEnum(chat.status));
            cv.put("chat_from_name",chat.from_name);
            cv.put("chat_to_name",chat.to_name);

            return sqLiteDatabase.insert("chat",null,cv);
        }
        return -1;
    }

    public boolean updateChat(Chat chat) {
        if(open()) {
            ContentValues cv=new ContentValues();
            cv.put("chat_propagation_type",Chat.fromEnum(chat.propagationType));
            cv.put("chat_from_id",chat.from_id);
            cv.put("chat_to_id",chat.to_id);
            cv.put("chat_timestamp",chat.timestamp);
            if(chat.security_data!=null) {
                cv.put("chat_security_data", chat.security_data);
            } else {
                cv.putNull("chat_security_data");
            }
            cv.put("chat_message_type",Chat.fromEnum(chat.message_type));
            cv.put("chat_message",chat.message.toString());
            cv.put("chat_status",Chat.fromEnum(chat.status));
            cv.put("chat_from_name",chat.from_name);
            cv.put("chat_to_name",chat.to_name);

            return sqLiteDatabase.update("chat",cv,"chat_id=?",new String[] {chat.id+""})>0;
        }
        return false;
    }

    public boolean updateOrAddChat(Chat chat) {
        if(open()) {
            Cursor cursor=sqLiteDatabase.rawQuery("SELECT EXISTS (SELECT * FROM chat where chat_id=?)",new String[] {chat.id+""});
            if(cursor.moveToFirst()) {
                boolean isExists=cursor.getLong(0)>0;

                if(isExists) {
                    return updateChat(chat);
                } else {
                    return addChat(chat)>=0;
                }
            }
        }
        return false;
    }

    public List<Chat> getChats(Chat.PropagationType propagationType, Chat.Status status,  long fromId, long toId) {
        List<Chat> chats=new ArrayList<Chat>();
        if(open()) {
            String query="select * from chat where 1=1 ";

            if(propagationType!= Chat.PropagationType.None) {
                query+="and chat_propagation_type="+Chat.fromEnum(propagationType)+" ";
            }

            if(status!= Chat.Status.None) {
                query+="and chat_status="+Chat.fromEnum(status)+" ";
            }

            if(fromId>-1 && toId>-1) {
                query+="and (chat_from_id="+fromId+" or chat_from_id="+toId+") ";
                query+="and (chat_to_id="+fromId+" or chat_to_id="+toId+") ";
            }

            //if(toId>-1) {
            //    query+="and chat_to_id="+toId+" ";
            //}

            if(propagationType== Chat.PropagationType.None && status== Chat.Status.None && fromId<0 && toId<0)
                query+="and chat_id=0";

            Cursor cursor=sqLiteDatabase.rawQuery(query,null);
            if(cursor.moveToFirst()) {
                do {
                    Chat chat=new Chat(cursor);
                    if(chat.ready) {
                        chat.isReceived=chat.to_id==toId;
                        chats.add(chat);
                    }
                } while (cursor.moveToNext());
            }
        }
        return chats;
    }

    public List<ChatHead> getChatHeads(long skipFromID) {
        List<ChatHead> chatHeads=new ArrayList<ChatHead>();

        if(open()) {

            // first run for personal messages eg. for propagation_type=unicast
            // second run for group messages eg. for propagation_type=multicast
            // third run for group messages eg. for propagation_type=broadcast

            // every run
            // group by fromId and unicast/multicast/broadcast

            String query="select sum(chat_status) as newCount, chat.* from chat where chat_from_id!="+skipFromID+" and chat_propagation_type="+Chat.fromEnum(Chat.PropagationType.Unicast)+" group by chat_from_id order by newCount";
            Cursor cursor=sqLiteDatabase.rawQuery(query,null);
            if(cursor.moveToFirst()) {
                do {
                    ChatHead head=new ChatHead();
                    Chat chat=new Chat(cursor);
                    if(chat.ready) {
                        head.fromId = chat.from_id;
                        head.toId = chat.to_id;
                        head.fromName = chat.from_name;
                        head.toName = chat.to_name;
                        head.newCount = cursor.getLong(cursor.getColumnIndex("newCount"));
                        head.messageType=chat.message_type;

                        if(chat.message!=null && chat.message.verify()) {
                           if(chat.message.text!=null) {
                               String msg=chat.message.text;
                               if(msg.length()>30) {
                                   msg=msg.substring(0,29);
                               }
                               head.messageLine=msg;
                           }
                        }

                        chatHeads.add(head);
                        chat=null;
                    }
                } while (cursor.moveToNext());
            }
        }

        return chatHeads;
    }


    /* ------------------------------------ CHAT ------------------------------------------------ */

    /************************************ COLLEGES ****************************************************/

    public List<College> getColleges() {
        List<College> colleges=new ArrayList<College>();
        if(open()) {
            Cursor cursor=sqLiteDatabase.rawQuery("select * from college",null);
            if(cursor.moveToFirst()) {
                do {
                    College college=new College(cursor);
                    if(college.ready) {
                        colleges.add(college);
                    }
                } while (cursor.moveToNext());
            }
        }
        return colleges;
    }

    public College getCollege(long id) {
        try {
            if(open()) {
                Cursor cursor=sqLiteDatabase.rawQuery("select * from college where college_id=?",new String[] {id+""});
                if(cursor.moveToFirst()) {
                    College college=new College(cursor);
                    if(college.ready)
                        return college;
                }
            }
        } catch (Exception ex) {
            Helper.Log(ex,"getCollege: "+id);
        }
        return null;
    }

    public void addColleges(List<College> colleges) {
        for(int i=0;i<colleges.size();i++) {
            addCollege(colleges.get(i));
        }
    }

    public long addCollege(College college) {
        if(open()) {
            ContentValues cv=new ContentValues();
            cv.put("college_id",college.id);
            cv.put("college_short_name",college.short_name);
            cv.put("college_full_name",college.full_name);
            cv.put("college_city", college.city);
            cv.put("college_fax_number",college.fax_number);
            cv.put("college_email_address",college.email_address);
            cv.put("college_telephone_number",college.telephone_number);
            cv.put("college_website",college.website);
            //skip logo
            cv.put("college_code",college.code);

            cv.put("college_address",college.address);
            cv.put("college_type",college.type);
            cv.put("college_estd_year",college.estd_year);
            cv.put("college_tution_fees",college.tution_fees);

            return sqLiteDatabase.insert("college",null,cv);
        }
        return -1;
    }

    public void clearColleges() {
        if(open()) {
            sqLiteDatabase.execSQL("delete from college");
        }
    }

    public boolean updateCollege(College college) {
        if(open()) {
            ContentValues cv=new ContentValues();
            cv.put("college_short_name",college.short_name);
            cv.put("college_full_name",college.full_name);
            cv.put("college_city", college.city);
            cv.put("college_fax_number",college.fax_number);
            cv.put("college_email_address",college.email_address);
            cv.put("college_telephone_number",college.telephone_number);
            cv.put("college_website",college.website);
            cv.put("college_code",college.code);

            cv.put("college_address",college.address);
            cv.put("college_type",college.type);
            cv.put("college_estd_year",college.estd_year);
            cv.put("college_tution_fees",college.tution_fees);

            return sqLiteDatabase.update("college",cv,"college_id="+college.id,null)>=0;
        }
        return false;
    }

    /************************************ COLLEGES ****************************************************/

    /************************************ ACCOUNTS ****************************************************/

    public Account getAccount(long id) {
        try {
            if(open()) {
                Cursor cursor=sqLiteDatabase.rawQuery("select * from account where account_id=?",new String[] {id+""});
                if(cursor.moveToFirst()) {
                    Account account=new Account(cursor);
                    if(account.ready)
                        return account;
                }
            }
        } catch (Exception ex) {
            Helper.Log(ex,"getAccount: "+id);
        }
        return null;
    }

    public long addAccount(Account account) {
        if(open()) {
            ContentValues cv=new ContentValues();
            cv.put("account_id",account.id);
            String type="";
            if(account.type== Account.AccountType.Faculty) {
                type="faculty";
            } else if(account.type== Account.AccountType.Student) {
                type="student";
            }
            cv.put("account_type",type);
            cv.put("account_name",account.name);
            cv.put("account_user_id",account.user_id); //must encrypt
            cv.put("account_email_address",account.email_address);
            cv.put("account_mobile_number",account.mobile_number);
            cv.put("account_activation_status",account.activation_status); // may not required
            cv.put("account_college_id",account.college_id);

            return sqLiteDatabase.insert("account",null,cv);
        }
        return -1;
    }

    public boolean updateAccount(Account account) {
        if(open()) {
            ContentValues cv=new ContentValues();
            String type="";
            if(account.type== Account.AccountType.Faculty) {
                type="faculty";
            } else if(account.type== Account.AccountType.Student) {
                type="student";
            }
            cv.put("account_type",type);
            cv.put("account_name",account.name);
            cv.put("account_user_id",account.user_id); //must encrypt
            cv.put("account_email_address",account.email_address);
            cv.put("account_mobile_number",account.mobile_number);
            cv.put("account_activation_status",account.activation_status); // may not required
            cv.put("account_college_id",account.college_id);

            return sqLiteDatabase.update("account",cv,"account_id="+account.id,null)>=0;
        }
        return false;
    }

    public boolean updateOrAddAccount(Account account) {
        if(open()) {
            Cursor cursor=sqLiteDatabase.rawQuery("SELECT EXISTS (SELECT * FROM account where account_id=?)",new String[] {account.id+""});
            if(cursor.moveToFirst()) {
                boolean isExists=cursor.getLong(0)>0;

                if(isExists) {
                    return updateAccount(account);
                } else {
                    return addAccount(account)>=0;
                }
            }
        }
        return false;
    }

    /************************************ ACCOUNTS ****************************************************/


    /************************************ PROFILES ****************************************************/

    public Profile getProfile(long profile_id, long account_id) {
        try {
            if(open()) {
                long selectedId=0;
                String query="select * from profile where ";
                if(profile_id>0) {
                    query+="profile_id=?";
                    selectedId=profile_id;
                } else if(account_id>0) {
                    query+="profile_account_id=?";
                    selectedId=account_id;
                } else {
                    query="profile_id=?";
                }

                Cursor cursor=sqLiteDatabase.rawQuery(query,new String[] {selectedId+""});
                if(cursor.moveToFirst()) {
                    Profile profile=new Profile(cursor);
                    if(profile.ready)
                        return profile;
                }
            }
        } catch (Exception ex) {
            Helper.Log(ex,"getProfile: profileId="+profile_id+", account_id="+account_id);
        }
        return null;
    }

    public long addProfile(Profile profile) {
        if(open()) {
            ContentValues cv=new ContentValues();
            cv.put("profile_id",profile.id);
            cv.put("profile_account_id",profile.account_id);
            cv.put("profile_first_name",profile.first_name);
            cv.put("profile_last_name",profile.last_name);
            cv.put("profile_mobile_number",profile.mobile_number);
            cv.put("profile_email_address",profile.email_address);
            cv.put("profile_timeline_status",profile.timeline_status);
            String sqlDate=Helper.Util.dateToSQLString(profile.dateofbirth);
            if(sqlDate!=null) {
                cv.put("profile_dateofbirth",sqlDate);
            } else {
                cv.putNull("profile_dateofbirth");
            }
            cv.put("profile_address",profile.address);
            cv.put("profile_gender",profile.fromGender(profile.gender));
            cv.put("profile_semester",profile.semester);

            return sqLiteDatabase.insert("profile",null,cv);
        }
        return -1;
    }

    public boolean updateProfile(Profile profile) {
        if(open()) {
            ContentValues cv=new ContentValues();
            cv.put("profile_account_id",profile.account_id);
            cv.put("profile_first_name",profile.first_name);
            cv.put("profile_last_name",profile.last_name);
            cv.put("profile_mobile_number",profile.mobile_number);
            cv.put("profile_email_address",profile.email_address);
            cv.put("profile_timeline_status",profile.timeline_status);
            String sqlDate=Helper.Util.dateToSQLString(profile.dateofbirth);
            if(sqlDate!=null) {
                cv.put("profile_dateofbirth",sqlDate);
            } else {
                cv.putNull("profile_dateofbirth");
            }
            cv.put("profile_address",profile.address);
            cv.put("profile_gender",profile.fromGender(profile.gender));
            cv.put("profile_semester",profile.semester);

            return sqLiteDatabase.update("profile",cv,"profile_id="+profile.id,null)>=0;
        }
        return false;
    }

    public boolean updateOrAddProfile(Profile profile) {
        if(open()) {
            Cursor cursor=sqLiteDatabase.rawQuery("SELECT EXISTS (SELECT * FROM profile where profile_id=?)",new String[] {profile.id+""});
            if(cursor.moveToFirst()) {
                boolean isExists=cursor.getLong(0)>0;

                if(isExists) {
                    return updateProfile(profile);
                } else {
                    return addProfile(profile)>=0;
                }
            }
        }
        return false;
    }

    public boolean deleteProfile(long profile_id, long account_id) {
        try {

            if(open()) {
                long id=0;
                String where = "";
                if (profile_id > 0) {
                    where = "profile_id=?";
                    id=profile_id;
                } else if (account_id > 0) {
                    where = "profile_account_id=?";
                    id=account_id;
                } else {
                    where = "profile_id=?";
                    id=0;
                }
                return sqLiteDatabase.delete("profile",where,new String[] {id+""})>0;

            }

        } catch (Exception ex) {
            Helper.Log(ex,"deleteProfile: "+profile_id+", "+account_id);
        }
        return false;
    }

    /************************************ PROFILES ****************************************************/


    /************************************ TIMELINES ****************************************************/

    public List<Timeline> getTimelines(long collegeId, long profileId) {
        List<Timeline> timelines=new ArrayList<Timeline>();
        if(open()) {
            String where="";
            if(collegeId>0) {
                where="where timeline_college_id="+collegeId;
            } else if(profileId>0) {
                where="where timeline_profile_id="+profileId;
            }
            Cursor cursor=sqLiteDatabase.rawQuery("select * from timeline "+where+" order by timeline_id desc",null);
            if(cursor.moveToFirst()) {
                do {
                    Timeline timeline=new Timeline(cursor);
                    if(timeline.ready) {
                        timelines.add(timeline);
                    }
                } while (cursor.moveToNext());
            }
        }
        return timelines;
    }

    public Timeline getTimeline(long id) {
        try {
            if(open()) {
                Cursor cursor=sqLiteDatabase.rawQuery("select * from timeline where timeline_id=?",new String[] {id+""});
                if(cursor.moveToFirst()) {
                    Timeline timeline=new Timeline(cursor);
                    if(timeline.ready)
                        return timeline;
                }
            }
        } catch (Exception ex) {
            Helper.Log(ex,"getTimeline: "+id);
        }
        return null;
    }

    public long addTimeline(Timeline timeline) {
        if(open()) {
            ContentValues cv=new ContentValues();
            cv.put("timeline_id",timeline.id);
            cv.put("timeline_profile_id",timeline.profile_id);
            cv.put("timeline_college_id",timeline.college_id);
            cv.put("timeline_type",Timeline.filterToInt(timeline.type));
            cv.put("timeline_timestamp",timeline.timestamp);

            if(timeline.security_data==null) {
                cv.putNull("timeline_security_data");
            } else {
                cv.put("timeline_security_data", timeline.security_data.toString());
            }

            if(timeline.data==null) {
                cv.putNull("timeline_data");
            } else {
                cv.put("timeline_data", timeline.data.toString());
            }

            return sqLiteDatabase.insert("timeline",null,cv);
        }
        return -1;
    }

    public boolean updateTimeline(Timeline timeline) {
        if(open()) {
            ContentValues cv=new ContentValues();
            cv.put("timeline_profile_id",timeline.profile_id);
            cv.put("timeline_college_id",timeline.college_id);
            cv.put("timeline_type",Timeline.filterToInt(timeline.type));
            cv.put("timeline_timestamp",timeline.timestamp);

            if(timeline.security_data==null) {
                cv.putNull("timeline_security_data");
            } else {
                cv.put("timeline_security_data", timeline.security_data.toString());
            }

            if(timeline.data==null) {
                cv.putNull("timeline_data");
            } else {
                cv.put("timeline_data", timeline.data.toString());
            }

            return sqLiteDatabase.update("timeline",cv,"timeline_id="+timeline.id,null)>=0;
        }
        return false;
    }

    public boolean updateOrAddTimeline(Timeline timeline) {
        if(open()) {
            Cursor cursor=sqLiteDatabase.rawQuery("SELECT EXISTS (SELECT * FROM timeline where timeline_id=?)",new String[] {timeline.id+""});
            if(cursor.moveToFirst()) {
                boolean isExists=cursor.getLong(0)>0;

                if(isExists) {
                    return updateTimeline(timeline);
                } else {
                    return addTimeline(timeline)>=0;
                }
            }
        }
        return false;
    }

    /************************************ TIMELINES ****************************************************/


    public void close() {
        if(sqLiteDatabase.isOpen())
            sqLiteDatabase.close();
    }

}
