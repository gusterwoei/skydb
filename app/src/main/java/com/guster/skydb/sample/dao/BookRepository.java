package com.guster.skydb.sample.dao;

import com.guster.skydb.Repository;
import com.guster.skydb.sample.domain.Book;

/**
 * Created by Gusterwoei on 15/11/2016.
 */
public class BookRepository extends Repository<Book> {

   public BookRepository() {
      super(Book.class);
   }

}
