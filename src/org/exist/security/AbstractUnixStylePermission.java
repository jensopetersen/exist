package org.exist.security;
/*
 *  eXist Open Source Native XML Database
 *  Copyright (C) 2001-2011 The eXist-db Project
 *  http://exist-db.org
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2
 *  of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 *
 *  $Id: UnixStylePermission.java 14571 2011-05-29 12:34:48Z deliriumsky $
 */
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.log4j.Logger;
import org.exist.util.SyntaxException;

/**
 * All code in this class must be side-effect free
 * and not carry state, thus ensuring that thus class
 * can be used in a local or remote scenario
 *
 * @author Adam Retter <adam@exist-db.org>
 */
public abstract class AbstractUnixStylePermission implements Permission {

    private final static Logger LOG = Logger.getLogger(AbstractUnixStylePermission.class);

    /**
     * The symbolic mode is described by the following grammar:
     *
     * mode         ::= clause [, clause ...]
     * clause       ::= [who ...] [action ...] action
     * action       ::= op [perm ...]
     * who          ::= a | u | g | o
     * op           ::= + | - | =
     * perm         ::= r | s | t | w | x
     */
    private void setUnixSymbolicMode(String symbolicMode) throws SyntaxException, PermissionDeniedException {

        //TODO expand perm to full UNIX chmod i.e. perm ::= r | s | t | w | x | X | u | g | o

        String clauses[] = symbolicMode.split(",");
        for(String clause : clauses) {
            String whoPerm[] = clause.split("[+-=]");

            int perm = 0;
            boolean uidgid = false;
            boolean sticky = false;
            //process the op first
            for(char c : whoPerm[1].toCharArray()) {
                switch(c) {
                    case READ_CHAR:
                        perm |= READ;
                        break;

                    case WRITE_CHAR:
                        perm |= WRITE;
                        break;

                    case EXECUTE_CHAR:
                        perm |= EXECUTE;
                        break;

                    case SETUID_CHAR:
                        uidgid = true;
                        break;

                    case STICKY_CHAR:
                        sticky = true;
                        break;

                    default:
                        throw new SyntaxException("Unrecognised mode char '" + c + "'");
                }
            }

            for(char c: whoPerm[0].toCharArray()) {
                switch(c) {
                    case ALL_CHAR:
                        int newMode = (perm << 6) | (perm << 3) | perm | (sticky ? (STICKY << 9) : 0) | (uidgid ? ((SET_UID | SET_GID) << 9) : 0);
                        if(clause.indexOf("+") > -1) {
                            setMode(getMode() | newMode);
                        } else if(clause.indexOf("-") > -1) {
                            setMode(getMode() & ~newMode);
                        } else if(clause.indexOf("=") > -1) {
                            setMode(newMode);
                        }
                        break;

                    case USER_CHAR:
                        if(clause.indexOf("+") > -1) {
                            setOwnerMode(getOwnerMode() | perm);
                            if(uidgid) {
                                setSetUid(true);
                            }
                        } else if(clause.indexOf("-") > -1) {
                            setOwnerMode(getOwnerMode() & ~perm);
                            if(uidgid) {
                                setSetUid(false);
                            }
                        } else if(clause.indexOf("=") > -1) {
                            setOwnerMode(perm);
                            if(uidgid) {
                                setSetUid(true);
                            }
                        }
                        break;

                    case GROUP_CHAR:
                        if(clause.indexOf("+") > -1) {
                            setGroupMode(getGroupMode() | perm);
                            if(uidgid) {
                                setSetGid(true);
                            }
                        } else if(clause.indexOf("-") > -1) {
                            setGroupMode(getGroupMode() & ~perm);
                            if(uidgid) {
                                setSetGid(false);
                            }
                        } else if(clause.indexOf("=") > -1) {
                            setGroupMode(perm);
                            if(uidgid) {
                                setSetGid(true);
                            }
                        }
                        break;

                    case OTHER_CHAR:
                        if(clause.indexOf("+") > -1) {
                            setOtherMode(getOtherMode() | perm);
                            if(sticky) {
                                setSticky(true);
                            }
                        } else if(clause.indexOf("-") > -1) {
                            setOtherMode(getOtherMode() & ~perm);
                            if(sticky) {
                                setSticky(false);
                            }
                        } else if(clause.indexOf("=") > -1) {
                            setOtherMode(perm);
                            if(sticky) {
                                setSticky(true);
                            }
                        }
                        break;

                    default:
                        throw new SyntaxException("Unrecognised mode char '" + c + "'");
                }
            }

            perm = 0;
            uidgid = false;
            sticky = false;
        }
    }

    /**
     *  Set mode using a string. The string has the
     * following syntax:
     *
     * [user|group|other]=[+|-][read|write|execute]
     *
     * For example, to set read and write mode for the group, but
     * not for others:
     *
     * group=+read,+write,other=-read,-write
     *
     * The new settings are or'ed with the existing settings.
     *
     *@param  existSymbolicMode                  The new mode
     *@exception  SyntaxException  Description of the Exception
     *
     * @deprecated setUnixSymbolicMode should be used instead
     */
    @Deprecated
    private void setExistSymbolicMode(String existSymbolicMode) throws SyntaxException, PermissionDeniedException {

        LOG.warn("Permission modes should not be set using this format '" + existSymbolicMode + "', consider using the UNIX symbolic mode instead");

        int shift = 0;
        int mode = getMode();
        for(String s : existSymbolicMode.toLowerCase().split("=|,")){
            if(s.equalsIgnoreCase(USER_STRING)) {
                shift = 6;
            } else if(s.equalsIgnoreCase(GROUP_STRING)) {
                shift = 3;
            } else if(s.equalsIgnoreCase(OTHER_STRING)) {
                shift = 0;
            } else {
                int perm = 0;

                if(s.endsWith(READ_STRING.toLowerCase())) {
                    perm = READ;
                } else if(s.endsWith(WRITE_STRING.toLowerCase())) {
                    perm = WRITE;
                } else if(s.endsWith(EXECUTE_STRING.toLowerCase())) {
                    perm = EXECUTE;
                } else {
                    throw new SyntaxException("Unrecognised mode char '" + s + "'");
                }

                if(s.startsWith("+")) {
                    mode |= (perm << shift);
                } else if(s.startsWith("-")) {
                    mode &= (~(perm << shift));
                } else {
                    throw new SyntaxException("Unrecognised mode char '" + s + "'");
                }
            }
        }
        setMode(mode);
    }

    /**
     * Simple symbolic mode is [rwxs-]{3}[rwxs-]{3}[rwxt-]{3}
     */
    private void setSimpleSymbolicMode(String simpleSymbolicMode) throws SyntaxException, PermissionDeniedException {

        int mode = 0;

        char modeArray[] = simpleSymbolicMode.toCharArray();
        for(int i = 0; i < modeArray.length; i++) {

            char c = modeArray[i];
            int shift = (i < 3 ? 6 : (i < 6 ? 3 : 0));

            switch(c) {
                case READ_CHAR:
                    mode |= (READ << shift);
                    break;
                case WRITE_CHAR:
                    mode |= (WRITE << shift);
                    break;
                case EXECUTE_CHAR:
                    mode |= (EXECUTE << shift);
                    break;
                case SETUID_CHAR:
                    if(i < 3) {
                        mode |= (SET_UID << 9);
                    } else {
                        mode |= (SET_GID << 9);
                    }
                    break;
                case STICKY:
                    mode |= (STICKY << 9);
                    break;

                case UNSET_CHAR:
                    break;

                default:
                    throw new SyntaxException("Unrecognised mode char '" + c + "'");
            }
        }
        setMode(mode);
    }

    private final static Pattern unixSymbolicModePattern = Pattern.compile("((?:[augo]+(?:[+-=](?:[" + READ_CHAR + SETUID_CHAR + STICKY_CHAR + WRITE_CHAR + EXECUTE_CHAR +"])+)+),?)+");
    private final static Matcher unixSymbolicModeMatcher = unixSymbolicModePattern.matcher("");

    private final static Pattern existSymbolicModePattern = Pattern.compile("(?:(?:" + USER_STRING + "|" + GROUP_STRING + "|" + OTHER_STRING + ")=(?:[+-](?:" + READ_STRING + "|" + WRITE_STRING + "|" + EXECUTE_STRING + "),?)+)+");
    private final static Matcher existSymbolicModeMatcher = existSymbolicModePattern.matcher("");

    private final static Pattern simpleSymbolicModePattern = Pattern.compile("[" + READ_CHAR + WRITE_CHAR + EXECUTE_CHAR + SETUID_CHAR + UNSET_CHAR + "]{3}[" + READ_CHAR + WRITE_CHAR + EXECUTE_CHAR + SETGID_CHAR + UNSET_CHAR + "]{3}[" + READ_CHAR + WRITE_CHAR + EXECUTE_CHAR + STICKY_CHAR + UNSET_CHAR + "]{3}");
    private final static Matcher simpleSymbolicModeMatcher = simpleSymbolicModePattern.matcher("");

    /**
     * Note we dont need @PermissionRequired(user = IS_DBA | IS_OWNER) here
     * because all of these methods delegate to the subclass implementation.
     */
    @Override
    public final void setMode(String modeStr) throws SyntaxException, PermissionDeniedException {
        simpleSymbolicModeMatcher.reset(modeStr);

        if(simpleSymbolicModeMatcher.matches()) {
            setSimpleSymbolicMode(modeStr);
        } else {
            unixSymbolicModeMatcher.reset(modeStr);
            if(unixSymbolicModeMatcher.matches()){
                setUnixSymbolicMode(modeStr);
            } else {
                existSymbolicModeMatcher.reset(modeStr);
                if(existSymbolicModeMatcher.matches()) {
                    setExistSymbolicMode(modeStr);
                } else {
                    throw new SyntaxException("Unknown mode String: " + modeStr);
                }
            }
        }
    }
}