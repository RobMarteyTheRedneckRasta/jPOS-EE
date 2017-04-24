/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2017 jPOS Software SRL
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.jpos.qi.eeuser;

import com.vaadin.v7.data.Validator;
import com.vaadin.v7.data.fieldgroup.BeanFieldGroup;
import com.vaadin.v7.data.fieldgroup.FieldGroup;
import com.vaadin.v7.data.util.BeanItem;
import com.vaadin.v7.ui.PasswordField;
import org.hibernate.Criteria;
import org.jpos.ee.*;
import org.jpos.qi.QIHelper;
import org.jpos.util.PasswordGenerator;

import java.util.*;
import java.util.List;
import java.util.stream.Stream;

public class UsersHelper extends QIHelper {

    public UsersHelper() {
        super(User.class);
    }

    @Override
    public Stream getAll(int offset, int limit, Map<String, Boolean> orders) throws Exception {
        List<User> all = (List<User>) DB.exec(db -> {
            UserManager mgr = new UserManager(db);
            return mgr.getAll(offset,limit,orders);
        });
        return all.stream();
    }

    @Override
    public int getItemCount() throws Exception {
        return (int) DB.exec(db -> {
            UserManager mgr = new UserManager(db);
            return mgr.getItemCount();
        });
    }

    @Override
    public String getItemId(Object item) {
        return String.valueOf(((User)item).getId());
    }

    public User getUserByNick (String nick, boolean includeDeleted) {
        try {
            return (User) DB.exec((db) -> {
                UserManager mgr = new UserManager(db);
                return mgr.getUserByNick(nick,includeDeleted);
            });
        } catch (Exception e) {
            getApp().getLog().error(e);
            return null;
        }
    }

    public boolean updateUser (BeanFieldGroup<User> fieldGroup, String currentPass, String newClearPass) throws BLException, CloneNotSupportedException {
        BeanItem<User> old = fieldGroup.getItemDataSource();
        Object oldUser = old.getBean().clone();
        try {
            fieldGroup.commit();
        } catch (FieldGroup.CommitException e) {
            e.printStackTrace();
        }
        BeanItem<User> item = fieldGroup.getItemDataSource();
        User u = item.getBean();
        boolean userUpdated = false;
        try {
            userUpdated = (boolean) DB.execWithTransaction((db) -> {
                User user = (User) db.session().merge(u);
                UserManager mgr = new UserManager(db);
                boolean updated = false;
                if (!newClearPass.isEmpty()) {
                    boolean passwordOK = false;
                    boolean newPasswordOK = false;
                    try {
                        passwordOK = mgr.checkPassword(user, currentPass);
                        newPasswordOK = mgr.checkNewPassword(user, newClearPass);
                        if (passwordOK && newPasswordOK) {
                            mgr.setPassword(user, newClearPass);
                            updated = true;
                        } else if (!newPasswordOK) {
                            throw new BLException("This password has already been used");
                        }
                    } catch (BLException e) {
                        // do nothing
                        return false;
                    }
                }
                updated = updated || addRevisionUpdated(db, getEntityName(),
                        String.valueOf(u.getId()),
                        oldUser,
                        u,
                        new String[]{"nick", "name", "email", "active", "roles", "password"});
                return updated;
            });
        } catch (Exception e) {
            getApp().getLog().error(e);
            return false;
        }
        if (userUpdated && u.equals(getApp().getUser())) {
            try {
                DB.exec((db)->{
                    db.session().refresh(getApp().getUser());
                    return null;
                });
            } catch (Exception e) {
                getApp().getLog().error(e);
            }
        }
        return userUpdated;
    }

    //Does not override SaveEntity because it needs the String clearPass
    public boolean saveUser (Object entity, String clearPass) throws BLException {
        User u = (User) entity;
        try {
            return (boolean) DB.execWithTransaction((db) -> {
                db.save(u);
                if (clearPass != null && !clearPass.isEmpty()) {
                    UserManager mgr = new UserManager(db);
                    try {
                        mgr.setPassword(u, clearPass);
                    } catch (BLException e) {
                        return false;
                    }
                    addRevisionCreated(db,getEntityName(), u.getId().toString());
                    u.setForcePasswordChange(true);
                    db.session().update(u);
                    return true;
                }
                return false;
            });
        } catch (Exception e) {
            getApp().getLog().error(e);
            return false;
        }
    }


    @Override
    public boolean removeEntity (BeanFieldGroup fieldGroup) {
        //Users have a deleted flag, they are not completely removed.
        BeanItem item = fieldGroup.getItemDataSource();
        User t = (User) item.getBean();
        try {
            return t != null && (boolean) DB.execWithTransaction((db) -> {
                User user = db.session().get(User.class, t.getId());
                if (user == null) return false;
                t.setDeleted(true);
                db.session().merge(t);
                addRevisionRemoved(db, getEntityName(), String.valueOf(t.getId()));
                return true;
            });
        } catch (Exception e) {
            getApp().getLog().error(e);
            return false;
        }
    }

    @Override
    public boolean updateEntity(BeanFieldGroup fieldGroup) throws BLException, CloneNotSupportedException {
        //NOT USED
        return false;
    }

    public List<Role> getRoles() {
        try {
            return (List<Role>) DB.exec((db) -> {
                Criteria crit = db.session().createCriteria(Role.class);
                return crit.list();
            });
        } catch (Exception e) {
            getApp().getLog().error(e);
            return null;
        }
    }

    public Validator getNickTakenValidator(final User selectedU) {

        return new Validator() {
            public boolean isValid(Object value) {
                String oldNick = selectedU.getNick();
                if (oldNick!= null) {
                    User u = getUserByNick((String)value,true);
                    return u == null || u.getId().equals(selectedU.getId());
                }
                else
                    return getUserByNick((String) value,true) == null;
            }

            public void validate(Object value) throws InvalidValueException {
                if (!isValid(value)) {
                    throw new InvalidValueException(getApp().getMessage(
                      "errorMessage.nickAlreadyExists", value)
                    );
                }
            }
        };
    }
    public Validator getPasswordsMatchValidator(final PasswordField newPass) {
        return (Validator) value -> {
            if (!newPass.getValue().equals(value))
                throw new Validator.InvalidValueException(getApp().getMessage("error.passwordsMatch"));
        };
    }

    public String resetUserPassword (User user) {
        String generatedPassword = PasswordGenerator.generateRandomPassword();
        try {
            DB.execWithTransaction((db) -> {
                db.session().refresh(user);
                user.getPasswordhistory(); // hack to avoid LazyInitialization
                UserManager mgr = new UserManager(db);
                try {
                    mgr.setPassword(user, generatedPassword);
                } catch (BLException e) {
                    getApp().displayNotification("errorMessage.resetPassword");
                    return false;
                }
                user.setForcePasswordChange(true);
                user.setLoginAttempts(0); // reset login attempts
                db.session().saveOrUpdate(user);
                return true;
            });
        } catch (Exception e) {
            getApp().getLog().error(e);
            return null;
        }
        return generatedPassword;
    }

    public Validator getCurrentPasswordMatchValidator (User user, final PasswordField currentPass) {
        return new Validator() {
            public boolean isValid (Object value) {
                try {
                    return (boolean) DB.exec((db) -> {
                        UserManager mgr = new UserManager(db);
                        try {
                            return mgr.checkPassword(user, (String) value);
                        } catch (BLException e) {
                            return false;
                        }
                    });
                } catch (Exception e) {
                    getApp().getLog().error(e);
                    return false;
                }
            }
            @Override
            public void validate(Object value) throws InvalidValueException {
                if (!isValid(value)) {
                    currentPass.focus();
                    throw new InvalidValueException(getApp().getMessage("error.invalidPassword"));
                }
            }
        };
    }
    public Validator getNewPasswordNotUsedValidator (User user, final PasswordField newPass) {
        return new Validator() {
            public boolean isValid (Object value) {
                try {
                    return (boolean) DB.exec((db) -> {
                        db.session().refresh(user);
                        UserManager mgr = new UserManager(db);
                        try {
                            return mgr.checkNewPassword(user, (String) value);
                        } catch (BLException e) {
                            return false;
                        }
                    });
                } catch (Exception e) {
                    getApp().getLog().error(e);
                    return false;
                }
            }
            @Override
            public void validate(Object value) throws InvalidValueException {
                if (!isValid(value)) {
                    newPass.focus();
                    throw new Validator.InvalidValueException(getApp().getMessage("error.passwordUsed"));
                }
            }
        };
    }
}
