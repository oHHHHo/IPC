// Book.aidl
package com.example.server;

import com.example.server.Book;
import com.example.server.AidlCallback;

interface BookManager {
    List<Book> getBooks();

    void addBook(in Book book);

    int add(int num1,int num2);

    //设置callback需添加--//
    void registerListener(AidlCallback callback);
    void unRegisterListener(AidlCallback callback);
    void doInBackGround();
    //------------------//
}
