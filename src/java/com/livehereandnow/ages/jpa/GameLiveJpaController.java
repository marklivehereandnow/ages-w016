/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.livehereandnow.ages.jpa;

import com.livehereandnow.ages.biz.GameLive;
import com.livehereandnow.ages.jpa.exceptions.NonexistentEntityException;
import com.livehereandnow.ages.jpa.exceptions.PreexistingEntityException;
import com.livehereandnow.ages.jpa.exceptions.RollbackFailureException;
import java.io.Serializable;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.transaction.UserTransaction;

/**
 *
 * @author mark
 */
public class GameLiveJpaController implements Serializable {

    public GameLiveJpaController(UserTransaction utx, EntityManagerFactory emf) {
        this.utx = utx;
        this.emf = emf;
    }
    private UserTransaction utx = null;
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(GameLive gameLive) throws PreexistingEntityException, RollbackFailureException, Exception {
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            em.persist(gameLive);
            utx.commit();
        } catch (Exception ex) {
            try {
                utx.rollback();
            } catch (Exception re) {
                throw new RollbackFailureException("An error occurred attempting to roll back the transaction.", re);
            }
            if (findGameLive(gameLive.getId()) != null) {
                throw new PreexistingEntityException("GameLive " + gameLive + " already exists.", ex);
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(GameLive gameLive) throws NonexistentEntityException, RollbackFailureException, Exception {
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            gameLive = em.merge(gameLive);
            utx.commit();
        } catch (Exception ex) {
            try {
                utx.rollback();
            } catch (Exception re) {
                throw new RollbackFailureException("An error occurred attempting to roll back the transaction.", re);
            }
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                Integer id = gameLive.getId();
                if (findGameLive(id) == null) {
                    throw new NonexistentEntityException("The gameLive with id " + id + " no longer exists.");
                }
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void destroy(Integer id) throws NonexistentEntityException, RollbackFailureException, Exception {
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            GameLive gameLive;
            try {
                gameLive = em.getReference(GameLive.class, id);
                gameLive.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The gameLive with id " + id + " no longer exists.", enfe);
            }
            em.remove(gameLive);
            utx.commit();
        } catch (Exception ex) {
            try {
                utx.rollback();
            } catch (Exception re) {
                throw new RollbackFailureException("An error occurred attempting to roll back the transaction.", re);
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<GameLive> findGameLiveEntities() {
        return findGameLiveEntities(true, -1, -1);
    }

    public List<GameLive> findGameLiveEntities(int maxResults, int firstResult) {
        return findGameLiveEntities(false, maxResults, firstResult);
    }

    private List<GameLive> findGameLiveEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(GameLive.class));
            Query q = em.createQuery(cq);
            if (!all) {
                q.setMaxResults(maxResults);
                q.setFirstResult(firstResult);
            }
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    public GameLive findGameLive(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(GameLive.class, id);
        } finally {
            em.close();
        }
    }

    public int getGameLiveCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<GameLive> rt = cq.from(GameLive.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
