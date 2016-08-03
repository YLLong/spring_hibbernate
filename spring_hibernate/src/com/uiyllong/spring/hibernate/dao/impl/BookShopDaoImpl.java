package com.uiyllong.spring.hibernate.dao.impl;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.uiyllong.spring.hibernate.dao.BookShopDao;
import com.uiyllong.spring.hibernate.exceptions.BookStockException;
import com.uiyllong.spring.hibernate.exceptions.UserAccountException;

@Repository("bookShopDao")
public class BookShopDaoImpl implements BookShopDao {

	@Autowired
	private SessionFactory sessionFactory;

	// 不推荐使用 HibernateTemplate 和 HibernateDaoSupport
	// 因为这样会导致 Dao 和 Spring 的 API 进行耦合
	// 可以移植性变差
	
	//获取和当前线程绑定的 Session. 
	public Session getSession() {
		return sessionFactory.getCurrentSession();
	}

	@Override
	public int findBookPriceByIsbn(String isbn) {
		String sql = "select b.price from Book b where b.isbn = ?";
		Query query = getSession().createQuery(sql).setParameter(0, isbn);
		return (int) query.uniqueResult();
	}

	@Override
	public void updateBookStock(String isbn) {
		//验证书的库存是否充足.
		String sql = "select b.stock from Book b where b.isbn = ?";
		int stock = (int) getSession().createQuery(sql).setParameter(0, isbn).uniqueResult();
		if (stock == 0) {
			throw new BookStockException("库存不足！");
		}
		String sql1 = "update Book b set b.stock = b.stock - 1 where b.isbn = ?";
		getSession().createQuery(sql1).setParameter(0, isbn).executeUpdate();
	}

	@Override
	public void updateUserAccount(String username, int price) {
		//验证余额是否足够
		String sql = "select a.balance from Account a where a.username = ?";
		int balance = (int) getSession().createQuery(sql).setParameter(0, username).uniqueResult();
		if (balance < price) {
			throw new UserAccountException("余额不足！");
		}
		String sql1 = "update Account a set a.balance = a.balance - ? where a.username = ?";
		getSession().createQuery(sql1).setParameter(0, price).setParameter(1, username).executeUpdate();
	}

}
