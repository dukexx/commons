package com.dukexx.boot.shiro.session;

import com.dukexx.boot.shiro.redis.RedisDao;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.session.Session;
import org.apache.shiro.session.UnknownSessionException;
import org.apache.shiro.session.mgt.eis.AbstractSessionDAO;

import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Set;

/**
 * @author dukexx
 * @date 2017/4/14
 * @since 1.0.0
 */
@Slf4j
@Getter
@Setter
public class DefaultRedisSessionDao extends AbstractSessionDAO implements Serializable{
    private static final long serialVersionUID = 1L;
    //cache session on local thread
    private final ThreadLocal<Session> sessionCache = new ThreadLocal();
    
    //session存储在redis里面的key前缀，如果是spring管理，则可配置
    protected String sessionPrefix="SHIRO_SESSION_KEY_";
    //redis访问
    protected RedisDao redisDao;

    public DefaultRedisSessionDao(RedisDao redisDao) {
        this.redisDao = redisDao;
    }
    
    /**
     * shiro在获取不到Session时，创建Session对象后调用create→doCreate
     * @param session
     * @return
     */
    @Override
    protected Serializable doCreate(Session session) {
        log.debug("doCreate arguments:session={}",session);
        //调用父类，生成sessionId
        Serializable sessionId= generateSessionId(session);
        //调用父类，将sessionId设置到session中
        assignSessionId(session,sessionId);
        saveSession(session);
        return session.getId();
    }

    @Override
    protected Session doReadSession(Serializable sessionId) {
        log.debug("doReadSession arguments:sessionId={}",sessionId);
        if (sessionId == null) {
            return null;
        }
        String sessionKey = sessionPrefix + sessionId;
        return (Session) redisDao.getObject(sessionKey);
    }

    @Override
    public void update(Session session) throws UnknownSessionException {
        log.debug("update arguments:sessionId={}, session={}",session.getId(),session);
        saveSession(session);
    }

    @Override
    public void delete(Session session) {
        log.debug("delete arguments:sessionId={}, session={}",session.getId(),session);
        String sessionKey = sessionPrefix + session.getId();
        redisDao.deleteObject(sessionKey);
    }

    @Override
    public Collection<Session> getActiveSessions() {
        Set<String> keys = redisDao.keys(sessionPrefix+"*");
        Collection<Session> sessions = new LinkedList<>();
        for (String key : keys) {
            Session session = (Session) redisDao.getObject(key);
            if (key != null) {
                sessions.add(session);
            }
        }
        log.debug("getActiveSession total={}",sessions.size());
        return sessions;
    }

    /**
     * 存储session
     * @param session
     */
    protected String saveSession(Session session) {
        log.debug("saveSession sessionId={}, session={}",session.getId(),session);
        if (session == null|| session.getId()==null) {
            log.debug("saveSession error cause by session={} and sessionId={}",session,session.getId());
            throw new RuntimeException("saveSession error cause by session or sessionId is null");
        }
        String sessionKey = createSessionKey(session);
        redisDao.saveObject(sessionKey,session,session.getTimeout());
        return sessionKey;
    }

    /**
     * 组合SessionKey
     * @param session
     * @return
     */
    protected String createSessionKey(Session session) {
        return this.sessionPrefix + session.getId();
    }

}
