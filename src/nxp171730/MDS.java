/**
 * Starter code for LP3
 *
 * @author Mihir Hindocha - mxh170027
 * @author Nihal Abdulla PT - nxp171730
 * @author Amal Mohan - axm179030
 * Multi-dimensional search:
 * Consider the web site of a seller like Amazon. They carry tens of thousands of products, and each product has many
 * attributes (Name, Size, Description, Keywords, Manufacturer, Price, etc.).
 * The search engine allows users to specify attributes of products that they are seeking, and shows products that have
 * most of those attributes. To make search efficient, the data is organized using appropriate data structures, such as
 * balanced trees. But, if products are organized by Name, how can search by price implemented efficiently?
 * The solution, called indexing in databases, is to create a new set of references to the objects for each search field,
 * and organize them to implement search operations on that field efficiently.  As the objects change, these access
 * structures have to be kept consistent.
 * <p>
 * In this project, each object has 3 attributes: id (long int), description (one or more long ints), and
 * price (dollars and cents). The following operations are supported:
 * <p>
 * Implement the operations using data structures that are best suited
 * for the problem.
 */

package nxp171730;

import java.util.*;

public class MDS {
    TreeMap<Long, Entry> idMap; //Key: ID, Value: Entry Object
    HashMap<Long, TreeSet<Long>> descMap; //Key Description, Value: TreeSet of ids

    /**
     * Entry class with properties
     * long id
     * TreeSet<Long> description
     * Money price
     */
    public class Entry {
        long id;
        TreeSet<Long> description;
        Money price;

        /**
         * Constructor for Entry class.
         *
         * @param id
         * @param description
         * @param price
         */
        Entry(long id, TreeSet description, Money price) {
            this.id = id;
            this.description = description;
            this.price = price;
        }

        /**
         * Getter method for price.
         *
         * @return Money
         */
        public Money getPrice() {
            return price;
        }

        /**
         * Setter method for price.
         *
         * @param price
         */
        public void setPrice(Money price) {
            this.price = price;
        }
    }

    /**
     * Constructor for MDS class.
     * Initializes idMap and descMap.
     */
    public MDS() {
        idMap = new TreeMap<>();
        descMap = new HashMap<>();
    }

    /**
     * Insert a new item whose description is given in the list.  If an entry with the same id already exists, then its
     * description and price are replaced by the new values, unless list is null or empty, in which case, just the price is updated.
     * Returns 1 if the item is new, and 0 otherwise.
     *
     * @param id
     * @param price
     * @param list
     * @return int
     */
    public int insert(long id, Money price, List<Long> list) {
        TreeSet<Long> description = new TreeSet<Long>(list);
        if (!idMap.containsKey(id)) {
            addElement(id, price, description);
            return 1;
        } else {
            if (description.isEmpty()) {
                updatePrice(id, price);
            } else {
                updateElement(id, price, description);
            }
            return 0;
        }
    }

    /**
     * Method defined to add new values in the idMap and HashMap.
     *
     * @param id
     * @param price
     * @param description
     */
    public void addElement(long id, Money price, TreeSet<Long> description) {
        idMap.put(id, new Entry(id, description, price));
        addToDescMap(id, description);
    }

    /**
     * Method defined to update the existing values in the idMap and HashMap.
     *
     * @param id          long
     * @param price       Money
     * @param description TreeSet<Long>
     */
    public void updateElement(long id, Money price, TreeSet<Long> description) {
        TreeSet<Long> oldDescription = getDescription(id);
        idMap.replace(id, new Entry(id, description, price));
        removeFromDescMap(id, oldDescription);
        addToDescMap(id, description);
    }

    /**
     * Method defined to update only the price of the element.
     *
     * @param id    long
     * @param price Money
     */
    public void updatePrice(long id, Money price) {
        TreeSet<Long> description = getDescription(id);
        idMap.replace(id, new Entry(id, description, price));
    }

    /**
     * Helper method to add values in the HashMap. It is used by the addElement method.
     *
     * @param id          long
     * @param description TreeSet<Long>
     */
    public void addToDescMap(Long id, TreeSet<Long> description) {
        for (long i : description) {
            if (descMap.containsKey(i)) {
                TreeSet<Long> oldSet = descMap.get(i);
                oldSet.add(id);
                descMap.replace(i, oldSet);
            } else {
                TreeSet<Long> newEntry = new TreeSet<>();
                newEntry.add(id);
                descMap.put(i, newEntry);
            }
        }
    }

    /**
     * Helper method to update values in the HashMap. It is used by the updateElement method.
     *
     * @param id             long
     * @param oldDescription TreeSet<Long>
     */
    private void removeFromDescMap(Long id, TreeSet<Long> oldDescription) {
        for (long i : oldDescription) {
            removeFromDescMap(id, i);
        }
    }

    /**
     * Helper method to remove id from descMap.
     *
     * @param id  long
     * @param des long
     */
    private void removeFromDescMap(Long id, Long des) {
        if (descMap.containsKey(des)) {
            TreeSet<Long> updateValues = descMap.get(des);
            updateValues.remove(id);
            if (updateValues.isEmpty()) {
                descMap.remove(des);
            } else {
                descMap.replace(des, updateValues);
            }
        }
    }

    /**
     * Return price of item with given id (or 0, if not found).
     *
     * @param id long
     * @return Money
     */
    public Money find(long id) {
        if (idMap.containsKey(id)) {
            return getPrice(id);
        }
        // Initializes d = 0 and c = 0. Since return type is Money cannot return 0 directly.
        return new Money(0, 0);
    }

    /**
     * Delete item from storage. Returns the sum of the long ints that are in the description of the item deleted,
     * or 0, if such an id did not exist.
     *
     * @param id long
     * @return long
     */
    public long delete(long id) {
        if (find(id).compareTo(new Money()) == 0) {
            return 0;
        } else {
            long sum = 0;
            TreeSet<Long> oldDescription = getDescription(id);
            Iterator<Long> iterator = oldDescription.iterator();
            while (iterator.hasNext()) {
                sum += iterator.next();
            }
            removeFromDescMap(id, oldDescription);
            idMap.remove(id);
            return sum;
        }
    }

    /**
     * Given a long int, find items whose description contains that number (exact match with one of the long ints in the
     * item's description), and return lowest price of those items. Return 0 if there is no such item.
     *
     * @param n long
     * @return Money
     */
    public Money findMinPrice(long n) {
        return findMinMaxPrice(n, -1);
    }

    /**
     * Given a long int, find items whose description contains that number, and return highest price of those items.
     * Return 0 if there is no such item.
     *
     * @param n long
     * @return Money
     */
    public Money findMaxPrice(long n) {
        return findMinMaxPrice(n, 1);
    }

    /**
     * Helper method for finding minimum and maximum price.
     *
     * @param n        long
     * @param maxOrMin int
     * @return Money
     */
    private Money findMinMaxPrice(long n, int maxOrMin) {
        if (!descMap.containsKey(n)) {
            return new Money();
        }
        TreeSet<Long> ids = descMap.get(n);
        Money result = new Money();
        boolean flag = false;
        for (Long id : ids) {
            Money cur = getPrice(id);
            if (cur.compareTo(result) == maxOrMin || !flag) {
                result = cur;
                flag = true;
            }
        }
        return result;
    }

    /**
     * Given a long int n, find the number of items whose description contains n, and in addition,
     * their prices fall within the given range, [low, high].
     *
     * @param n    long
     * @param low  Money
     * @param high Money
     * @return int
     */
    public int findPriceRange(long n, Money low, Money high) {
        if (descMap.containsKey(n)) {
            int sum = 0;
            TreeSet<Long> idS = descMap.get(n);
            for (Long id : idS) {
                if (idMap.containsKey(id)) {
                    Money price = getPrice(id);
                    if (price.compareTo(low) >= 0 && price.compareTo(high) <= 0) {
                        sum++;
                    }
                }
            }
            return sum;
        }
        return 0;
    }

    /**
     * Increase the price of every product, whose id is in the range [l,h] by r%.  Discard any fractional pennies in the new
     * prices of items. Returns the sum of the net increases of the prices.
     *
     * @param l    long
     * @param h    long
     * @param rate double
     * @return Money
     */
    public Money priceHike(long l, long h, double rate) {
        SortedMap<Long, Entry> subMap = idMap.subMap(l, true, h, true);
        double netSum = 0;
        for (Map.Entry<Long, Entry> mapEntry : subMap.entrySet()) {
            Entry entry = mapEntry.getValue();
            long priceC = entry.getPrice().moneyInCents();
            long increase = (long) (priceC * rate / 100.0);
            long updatedPriceC = priceC + increase;
            entry.setPrice(new Money((long) (updatedPriceC / 100), (int) (updatedPriceC % 100)));
            netSum += increase;
        }
        return new Money((long) (netSum / 100.0), (int) (netSum % 100));
    }

    /**
     * Getter for description given id.
     *
     * @param id long
     * @return description for an id
     */
    public TreeSet<Long> getDescription(Long id) {
        if (idMap.containsKey(id)) {
            return idMap.get(id).description;
        }
        return null;
    }

    /**
     * Remove elements of list from the description of id. It is possible that some of the items in the list are not in the
     * id's description. Return the sum of the numbers that are actually deleted from the description of id.
     * Return 0 if there is no such id.
     *
     * @param id   long
     * @param list List<Long>
     * @return long
     */
    public long removeNames(long id, List<Long> list) {
        TreeSet<Long> curList = getDescription(id);
        Long sum = 0L;
        for (Long des : list) {
            if (curList.contains(des)) {
                sum += des;
                removeFromDescMap(id, des);
                curList.remove(des);
            }
        }
        idMap.replace(id, new Entry(id, curList, getPrice(id)));
        return sum;
    }

    /**
     * Getter method for price given id.
     *
     * @param id long
     * @return Money
     */
    public Money getPrice(long id) {
        if (idMap.containsKey(id)) {
            return idMap.get(id).getPrice();
        }
        return null;
    }

    // Do not modify the Money class in a way that breaks LP3Driver.java.
    public static class Money implements Comparable<Money> {
        long d;
        int c;
        boolean singleDigitCents = false;

        public Money() {
            d = 0;
            c = 0;
        }

        public Money(long d, int c) {
            this.d = d;
            this.c = c;
        }

        public Money(String s) {
            String[] part = s.split("\\.");
            int len = part.length;
            if (len < 1) {
                d = 0;
                c = 0;
            } else if (part.length == 1) {
                d = Long.parseLong(s);
                c = 0;
            } else {
                d = Long.parseLong(part[0]);
                c = Integer.parseInt(part[1]);
            }
            singleDigitCents = c < 10 && c != 0;
        }

        /**
         * Returns the dollar part.
         *
         * @return long
         */
        public long dollars() {
            return d;
        }

        /**
         * Returns the cents part.
         *
         * @return int
         */
        public int cents() {
            return c;
        }

        /**
         * Returns the price in cents.
         *
         * @return long
         */
        public long moneyInCents() {
            return d * 100 + c;
        }

        /**
         * Compares two objects of class Money.
         *
         * @param other Money
         * @return int
         */
        public int compareTo(Money other) {
            if (this.dollars() > other.dollars()) {
                return 1;
            } else if (other.dollars() > this.dollars()) {
                return -1;
            } else if (this.cents() > other.cents()) {
                return 1;
            } else if (this.cents() < other.cents()) {
                return -1;
            } else {
                return 0;
            }
        }

        /**
         * Returns the price in string format.
         *
         * @return String
         */
        public String toString() {
            return d + "." + (singleDigitCents ? "0" + c : c);
        }
    }
}
