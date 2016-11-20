package com.guster.skydb.sample.domain;

import com.guster.skydb.Rule;
import com.guster.skydb.annotation.Column;
import com.guster.skydb.annotation.Table;

/**
 * Created by Gusterwoei on 15/11/2016.
 */
@Table(name = Book.TABLE_NAME)
public class Book {
   public static final String TABLE_NAME = "books";
   public static final String COL_ID = "id";
   public static final String COL_NAME = "name";

   @Column(name = COL_ID, notNull = true, primaryKey = true, autoIncrement = true, incrementRule = Rule.UUID)
   private String id;
   @Column(name = COL_NAME, notNull = true)
   private String name;

   public String getId() {
      return id;
   }

   public void setId(String id) {
      this.id = id;
   }

   public String getName() {
      return name;
   }

   public void setName(String name) {
      this.name = name;
   }
}
