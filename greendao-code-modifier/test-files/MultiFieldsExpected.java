package org.greenrobot.greendao.example;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;

@Entity
public class Note {

    @Id
    private Long id;

    private String one, two;

    @Generated(hash = 445306708)
    public Note(Long id, String one, String two) {
        this.id = id;
        this.one = one;
        this.two = two;
    }

    @Generated(hash = 1272611929)
    public Note() {
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOne() {
        return this.one;
    }

    public void setOne(String one) {
        this.one = one;
    }

    public String getTwo() {
        return this.two;
    }

    public void setTwo(String two) {
        this.two = two;
    }

}
