/*
 * Copyright 2002,2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.anodyneos.xp.tag.core;

import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;

import javax.servlet.jsp.el.ELException;

import org.anodyneos.xp.XpException;
import org.anodyneos.xp.XpOutput;
import org.anodyneos.xp.tagext.XpTagSupport;
import org.apache.commons.collections.iterators.ArrayIterator;
import org.apache.commons.collections.iterators.EnumerationIterator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.SAXException;

/**
 * Iterates over a collection, iterator or an array of objects. Uses the same
 * syntax as the <a href="http://java.sun.com/products/jsp/jstl/">JSTL </a>
 * <code>forEach</code> tag does.
 *
 * @author <a href="mailto:jstrachan@apache.org">James Strachan </a>
 * @version $Revision: 1.3 $
 */
public class ForEachTag extends XpTagSupport {

    protected static final Iterator EMPTY_ITERATOR = Collections.EMPTY_LIST
            .iterator();

    /** The Log to which logging calls will be made. */
    private static final Log log = LogFactory.getLog(ForEachTag.class);

    /** Holds the variable name to export for the item being iterated over. */
    private Object items;

    /**
     * If specified then the current item iterated through will be defined as
     * the given variable name.
     */
    private String var;

    /**
     * If specified then the current index counter will be defined as the given
     * variable name.
     */
    private String indexVar;

    /** variable to hold loop status */
    private String statusVar;

    /** The starting index value */
    private int begin;

    /** The ending index value */
    private int end = Integer.MAX_VALUE;

    /** The index increment step */
    private int step = 1;

    /** The iteration index */
    private int index;

    public ForEachTag() {
    }

    // Tag interface

    // -------------------------------------------------------------------------
    public void doTag(XpOutput output) throws XpException,
            SAXException, ELException {

        if (log.isDebugEnabled()) {
            log.debug("running with items: " + items);
        }

        if (items != null) {
            Iterator iter = toIterator(items);
            if (iter == null) {
                throw new XpException("Invalid type for attribute items.");
            }

            if (log.isDebugEnabled()) {
                log.debug("Iterating through: " + iter);
            }

            // ignore the first items of the iterator
            for (index = 0; index < begin && iter.hasNext(); index++) {
                iter.next();
            }

            // set up the status
            LoopStatus status = null;
            if (statusVar != null) {
                // set up statii as required by JSTL
                Integer statusBegin = (begin == 0) ? null : new Integer(begin);
                Integer statusEnd = (end == Integer.MAX_VALUE) ? null
                        : new Integer(end);
                Integer statusStep = (step == 1) ? null : new Integer(step);
                status = new LoopStatus(statusBegin, statusEnd, statusStep);
                getXpContext().setAttribute(statusVar, status);
            }

            boolean firstTime = true;
            int count = 0;
            while (iter.hasNext() && index <= end) {
                Object value = iter.next();
                if (var != null) {
                    getXpContext().setAttribute(var, value);
                }
                if (indexVar != null) {
                    getXpContext().setAttribute(indexVar, new Integer(index));
                }
                // set the status var up
                if (statusVar != null) {
                    count++;
                    status.setCount(count);
                    status.setCurrent(value);
                    status.setFirst(firstTime);
                    status.setIndex(index);
                    // set first time up for the next iteration.
                    if (firstTime) {
                        firstTime = !firstTime;
                    }
                }
                // now we need to work out the next index for status isLast
                // and also advance the iterator and index for the loop.
                boolean finished = false;
                index++;
                for (int i = 1; i < step && !finished; i++, index++) {
                    if (!iter.hasNext()) {
                        finished = true;
                    } else {
                        iter.next();
                    }
                }

                if (statusVar != null) {
                    status.setLast(finished || !iter.hasNext() || index > end);
                }
                if (null != getXpBody()) {
                    getXpBody().invoke(output);
                }

            }
        } else {
            if (end == Integer.MAX_VALUE && begin == 0) {
                throw new XpException("Attribute items is required.");
            } else {
                String varName = var;
                if (varName == null) {
                    varName = indexVar;
                }
                // set up the status
                LoopStatus status = null;
                if (statusVar != null) {
                    // set up statii as required by JSTL
                    Integer statusBegin = new Integer(begin);
                    Integer statusEnd = new Integer(end);
                    Integer statusStep = new Integer(step);
                    status = new LoopStatus(statusBegin, statusEnd, statusStep);
                    getXpContext().setAttribute(statusVar, status);
                }

                int count = 0;
                for (index = begin; index <= end; index += step) {

                    Object value = new Integer(index);
                    if (varName != null) {
                        getXpContext().setAttribute(varName, value);
                    }
                    // set the status var up
                    if (status != null) {
                        count++;
                        status.setIndex(index);
                        status.setCount(count);
                        status.setCurrent(value);
                        status.setFirst(index == begin);
                        status.setLast(index > end - step);
                    }
                    getXpBody().invoke(output);
                }
            }
        }
    }

    // Properties
    // -------------------------------------------------------------------------

    /**
     * Sets the expression used to iterate over. This expression could resolve
     * to an Iterator, Collection, Map, Array, Enumeration or comma separated
     * String.
     */
    public void setItems(Object items) {
        this.items = items;
    }

    /**
     * Sets the variable name to export for the item being iterated over
     */
    public void setVar(String var) {
        this.var = var;
    }

    /**
     * Sets the variable name to export the current index counter to
     */
    public void setIndexVar(String indexVar) {
        this.indexVar = indexVar;
    }

    /**
     * Sets the starting index value
     */
    public void setBegin(int begin) {
        this.begin = begin;
    }

    /**
     * Sets the ending index value
     */
    public void setEnd(int end) {
        this.end = end;
    }

    /**
     * Sets the index increment step
     */
    public void setStep(int step) {
        this.step = step;
    }

    /**
     * Sets the variable name to export the current status to. The status is an
     * implementation of the JSTL LoopTagStatus interface that provides the
     * following bean properties:
     * <ul>
     * <li>current - the current value of the loop items being iterated</li>
     * <li>index - the current index of the items being iterated</li>
     * <li>first - true if this is the first iteration, false otherwise</li>
     * <li>last - true if this is the last iteration, false otherwise</li>
     * <li>begin - the starting index of the loop</li>
     * <li>step - the stepping value of the loop</li>
     * <li>end - the end index of the loop</li>
     * </ul>
     */
    public void setVarStatus(String var) {
        this.statusVar = var;
    }

    /**
     * Holds the status of the loop.
     */
    public static final class LoopStatus implements LoopTagStatus {
        private Integer begin;

        private int count;

        private Object current;

        private Integer end;

        private int index;

        private Integer step;

        private boolean first;

        private boolean last;

        public LoopStatus(Integer begin, Integer end, Integer step) {
            this.begin = begin;
            this.end = end;
            this.step = step;
        }

        /**
         * @return Returns the begin.
         */
        public Integer getBegin() {
            return begin;
        }

        /**
         * @return Returns the count.
         */
        public int getCount() {
            return count;
        }

        /**
         * @return Returns the current.
         */
        public Object getCurrent() {
            return current;
        }

        /**
         * @return Returns the end.
         */
        public Integer getEnd() {
            return end;
        }

        /**
         * @return Returns the first.
         */
        public boolean isFirst() {
            return first;
        }

        /**
         * @return Returns the index.
         */
        public int getIndex() {
            return index;
        }

        /**
         * @return Returns the last.
         */
        public boolean isLast() {
            return last;
        }

        /**
         * @return Returns the step.
         */
        public Integer getStep() {
            return step;
        }

        /**
         * @param count
         *            The count to set.
         */
        public void setCount(int count) {
            this.count = count;
        }

        /**
         * @param current
         *            The current to set.
         */
        public void setCurrent(Object current) {
            this.current = current;
        }

        /**
         * @param first
         *            The first to set.
         */
        public void setFirst(boolean first) {
            this.first = first;
        }

        /**
         * @param last
         *            The last to set.
         */
        public void setLast(boolean last) {
            this.last = last;
        }

        /**
         * @param index
         *            The index to set.
         */
        public void setIndex(int index) {
            this.index = index;
        }
    }

    public Iterator toIterator(Object value) {
        if (value == null) {
            return EMPTY_ITERATOR;
        } else if (value.getClass().isArray()) {
            return new ArrayIterator(value);
        } else if (value instanceof Iterator) {
            return (Iterator) value;
        } else if (value instanceof Map) {
            Map map = (Map) value;
            return map.entrySet().iterator();
        } else if (value instanceof Enumeration) {
            return new EnumerationIterator((Enumeration) value);
        } else if (value instanceof Collection) {
            Collection collection = (Collection) value;
            return collection.iterator();
        } else if (value instanceof String) {
            StringTokenizer st = new StringTokenizer((String) value, ",");
            return new EnumerationIterator(st);
        } else {
            // TODO: make sure recipient throws exception "unsupported type"
            return null;
        }
    }

}
