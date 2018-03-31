package ec.edu.epn.fis.twitter.client.util;

//HandyDB
//Dynamic RMS-Datenbase-System
//by qioo_dev@qioo.com
//7.12.2005, Thomas Lucka 
import javax.microedition.rms.*;

/**
 * This class is compatible to J2ME MIDP 1.0/ CLDC 1.0 
 *
 * Database uses a second internal RMS-store to save an ID. 
 * Each EntryName is automatically unique otherwise you use an exiting one to 
 * overwrite data. 
 *
 * Entry names should not beginn with a number cause of ID.
 * And don't save empty Strings.
 * 	
 * Usage Examples: 
 * TEST IF ENTRY EXISTS: byte[] result = HandyDB.read("highscore1"); 
 *						if (new String(result)).equals(HandyDB.EMPTY)) {...}	
 *
 * READ ENTRY: byte[] result = HandyDB.read("highscore1"); 	
 * WRITE ENTRY: HandyDB.write("Highscore12", byteArray);
 * DELETE ENTRY: HandyDB.delete("Highscore12");	
 * 
 * LIST ALL ENTIES: String[] liste = HandyDB.showAll();
 * LIST SELECTED ENTRIES: String[] liste = HandyDB.showSelection("Highsco");
 * 
 * GET FREE SPACE: int freeBytes = HandyDB.getSizeAvailable(); 
 * 
 *  //Example:
 *	String[] liste = HandyDB.showAll();
 *	for (int i=0; i<liste.length; i++){ 
 *		String value="leer";
 *		if (liste[i]!=null) value=new String(HandyDB.read(liste[i]));
 *		System.out.println(i+": "+liste[i]+": "+value); 
 *	}
 *
 *
 * Example of data types:
 * byte, int, string, boolean
 * byte[] is default
 *
 * boolean b=true;
 * HandyDB.writeBoolean("thomas", b);
 * System.out.println(HandyDB.readBoolean("thomas"));
 *	
 * int i=678768;
 * HandyDB.writeInteger("thomas2", i);
 * System.out.println(HandyDB.readInteger("thomas2"));
 *			
 * byte by=67;
 * HandyDB.writeByte("thomas3", by);
 * System.out.println(HandyDB.readByte("thomas3"));
 *	
 * String str="Hello World";
 * HandyDB.writeString("thomas4", str);
 * System.out.println(HandyDB.readString("thomas4"));
 *	
 * //not existing entries 
 * System.out.println(HandyDB.readString("thomas5"));	//=> "EMPTY" 
 *
 *
 * Please note: Some Samsung phones have a bug and can not delete RMS-entries.
 * In this case add the method deleteAll() - so user can delete all entries. 
 *
 */
public class HandyDB {

//-----------------RMS-Database------------------------------------------------
    static RecordStore rs = null;
    static String store = "store";
    static RecordStore iDB = null;
    static String indexDB = "indexDB";
    static String EMPTY = "EMPTY";

//------------------------------------------------------------------------------
    public static synchronized byte[] read(String entryName) {

        byte[] result = EMPTY.getBytes();

        try {

            rs = RecordStore.openRecordStore(store, true); //create, wenn nicht voranden
            int id = getID(entryName);
            if (id != -1) {
                result = rs.getRecord(id);
            }
            rs.closeRecordStore();

        } catch (Exception e) {
            System.out.println("RMSError: " + e.toString());
        }

        return result;

    }

//--------Read-Helper-----------------------------------------------------------
    public static int readInteger(String entryName) {
        byte[] result = read(entryName);
        return byte2int(result);
    }

    public static boolean readBoolean(String entryName) {
        byte[] result = read(entryName);
        int dummy = byte2int(result);
        if (dummy == 1) {
            return true;
        } else {
            return false;
        }
    }

    public static String readString(String entryName) {
        byte[] result = read(entryName);
        return new String(result);
    }

    public static byte readByte(String entryName) {
        byte[] result = read(entryName);
        return result[0];
    }

//------------------------------------------------------------------------------	
    public static synchronized boolean write(String entryName, byte[] values) {

        try {

            rs = RecordStore.openRecordStore(store, true);
            int id = getID(entryName);
            if (id > 0) {
                rs.setRecord(id, values, 0, values.length);
            } else {
                id = rs.addRecord(values, 0, values.length);
                String newIdString = Integer.toString(id);
                byte[] newId = newIdString.getBytes();
                byte[] newName = entryName.getBytes();
                byte[] newIndexEntry = concatenateArrays(newId, newName);

                iDB = RecordStore.openRecordStore(indexDB, true);
                iDB.addRecord(newIndexEntry, 0, newIndexEntry.length);
                iDB.closeRecordStore();
            }

            rs.closeRecordStore();

        } catch (Exception e) {
            System.out.println("RMSError: " + e.toString());
            return false;
        }

        return true;

    }

//--------------Write-Helper----------------------------------------------------
    public static synchronized boolean writeInteger(String entryName, int values) {
        byte[] convertedValues = int2byte(values);
        return write(entryName, convertedValues);
    }

    public static synchronized boolean writeBoolean(String entryName, boolean values) {
        int dummy = 0; 		 //0 is false
        if (values) {
            dummy = 1; //1 is true
        }
        byte[] convertedValues = int2byte(dummy);
        return write(entryName, convertedValues);
    }

    public static synchronized boolean writeString(String entryName, String values) {
        byte[] convertedValues = values.getBytes();
        return write(entryName, convertedValues);
    }

    public static synchronized boolean writeByte(String entryName, byte values) {
        byte[] convertedValues = {values};
        return write(entryName, convertedValues);
    }

//------------------------------------------------------------------------------
    public static synchronized void delete(String entryName) {

        try {

            rs = RecordStore.openRecordStore(store, true);
            int id = getID(entryName);

            if (id > 0) {

                rs.deleteRecord(id);

                iDB = RecordStore.openRecordStore(indexDB, true);
                RecordEnumeration re = iDB.enumerateRecords(null, null, false);
                while (re.hasNextElement()) {
                    id = re.nextRecordId();
                    byte[] b = iDB.getRecord(id);
                    String ergebnis = new String(b);
                    if (ergebnis.indexOf(entryName) != -1) {
                        iDB.deleteRecord(id);
                    }
                }
                re.destroy();
                iDB.closeRecordStore();

            }

            rs.closeRecordStore();

        } catch (Exception e) {
            System.out.println("RMSError: " + e.toString());
        }

    }

//------------------------------------------------------------------------------
    private static synchronized int getID(String entryName) {
        int id = -1;
        try {

            iDB = RecordStore.openRecordStore(indexDB, true);
            RecordEnumeration re = iDB.enumerateRecords(null, null, false);
            while (re.hasNextElement()) {
                byte[] b = re.nextRecord();
                String ergebnis = new String(b);

                int index = ergebnis.indexOf(entryName);
                if (index != -1) {
                    String idStr = ergebnis.substring(0, index);
                    id = Integer.parseInt(idStr);
                }
            }
            re.destroy();
            iDB.closeRecordStore();
        } catch (Exception e) {
            System.out.println("RMSError: " + e.toString());
        }
        return id;
    }

//------------------------------------------------------------------------------
    public static synchronized String[] showAll() {
        String[] liste = null;
        try {

            iDB = RecordStore.openRecordStore(indexDB, true); //create, wenn nicht voranden
            RecordEnumeration re = iDB.enumerateRecords(null, null, false);
            int anzahlEntrys = iDB.getNumRecords();
            if (anzahlEntrys == 0) {
                liste = new String[1];
                liste[0] = EMPTY;
            } else {
                liste = new String[anzahlEntrys];
            }

            int indexListe = 0;
            while (re.hasNextElement()) {
                byte[] b = re.nextRecord();
                String eintrag = new String(b);

                int start = 0;
                char[] chars = eintrag.toCharArray();
                for (int i = 0; i < chars.length; i++) {
                    if (chars[i] < 48 || chars[i] > 57) { //0 - 9 = 48 - 57
                        start = i;
                        i = 9999999;
                    }
                }

                liste[indexListe] = eintrag.substring(start);
                indexListe++;
            }

            re.destroy();
            iDB.closeRecordStore();

        } catch (Exception e) {
            System.out.println("RMSError: " + e.toString());
        }
        return liste;
    }
//------------------------------------------------------------------------------
    public static synchronized String[] showSelection(String suchwort) {
        String[] liste = null;
        String[] ergebnisListe = null;
        try {
            iDB = RecordStore.openRecordStore(indexDB, true);
            RecordEnumeration re = iDB.enumerateRecords(null, null, false);
            int anzahlEntrys = iDB.getNumRecords();
            liste = new String[anzahlEntrys];

            int indexListe = 0;
            while (re.hasNextElement()) {
                byte[] b = re.nextRecord();
                String eintrag = new String(b);
                int index = eintrag.indexOf(suchwort);
                if (index != -1) {

                    int start = 0;
                    char[] chars = eintrag.toCharArray();
                    for (int i = 0; i < chars.length; i++) {
                        if (chars[i] < 48 || chars[i] > 57) { //0 - 9 <=> 48 - 57
                            start = i;
                            i = 9999999;
                        }
                    }

                    liste[indexListe] = eintrag.substring(start);
                    indexListe++;
                }
            }

            if (indexListe == 0) {
                ergebnisListe = new String[1];
                ergebnisListe[0] = EMPTY;
            } else {
                int anz = 0;
                for (int i = 0; i < liste.length; i++) {
                    if (liste[i] != null) {
                        anz++;
                    }
                }
                ergebnisListe = new String[anz];
                for (int i = 0; i < ergebnisListe.length; i++) {
                    if (liste[i] != null) {
                        ergebnisListe[i] = liste[i];
                    }
                }
            }

            re.destroy();
            iDB.closeRecordStore();

        } catch (Exception e) {
            System.out.println("RMSError: " + e.toString());
        }
        return ergebnisListe;
    }

//----------HHelper-------------------------------------------------------------
    public static synchronized void deleteAll() {
        try {
            RecordStore.deleteRecordStore(indexDB);
            RecordStore.deleteRecordStore(store);
            System.out.println("2x RMS deleted");
        } catch (Exception e) {
            System.out.println("RMSError: " + e.toString());
        }
    }

    public static synchronized int getStorageSize() {
        int size = 0;
        try {
            rs = RecordStore.openRecordStore(store, true);
            iDB = RecordStore.openRecordStore(store, true);

            size += rs.getSize();
            size += iDB.getSize();

            iDB.closeRecordStore();
            rs.closeRecordStore();
        } catch (Exception e) {
            System.out.println("RMSError: " + e.toString());
        }
        return size;
    }

    public static synchronized int getSizeAvailable() {
        int size = 0;
        try {
            rs = RecordStore.openRecordStore(store, true);
            size = rs.getSizeAvailable() - 500;
            rs.closeRecordStore();
        } catch (Exception e) {
            System.out.println("RMSError: " + e.toString());
        }
        return size;
    }

    public static synchronized void debug() {
        try {
            int anzEntrys;
            rs = RecordStore.openRecordStore(store, true);
            anzEntrys = rs.getNumRecords();
            System.out.println("Read Database, Entries:" + anzEntrys);

            iDB = RecordStore.openRecordStore(store, true);
            anzEntrys = iDB.getNumRecords();
            System.out.println("Read indexDB, Entries:" + anzEntrys);

            int sizeBelegt = getStorageSize();
            System.out.println("Used Space (DB):" + sizeBelegt);
            int freeBytes = getSizeAvailable();
            System.out.println("Free Space (DB):" + freeBytes);

            RecordEnumeration re = iDB.enumerateRecords(null, null, false);
            while (re.hasNextElement()) {
                byte[] b = re.nextRecord();
                String eintrag = new String(b);
                System.out.println("iDB->" + eintrag);
            }

            rs.closeRecordStore();
            iDB.closeRecordStore();
        } catch (Exception e) {
            System.out.println("RMS-Debug-Error: " + e.toString());
        }
    }

//-------------------------------------------------------------------------------
    public static byte[] concatenateArrays(byte[] array1, byte[] array2) {
        byte[] merge = new byte[array1.length + array2.length];
        System.arraycopy(array1, 0, merge, 0, array1.length);
        System.arraycopy(array2, 0, merge, array1.length, array2.length);
        return merge;
    }

//-------------------------CONVERT INT/BYTES------------------------------------
//MSBit first = Big Endian 
    public static byte[] int2byte(int v) {
        byte[] b = new byte[4];

        b[0] = (byte) (0xff & (v >> 24)); //MSB
        b[1] = (byte) (0xff & (v >> 16));
        b[2] = (byte) (0xff & (v >> 8));
        b[3] = (byte) (0xff & v);

        return b;

    }

//input 1-4 bytes and get resulting int, (int has max. 4 bytes)
    public static int byte2int(byte[] b) {

        if (b.length == 1) {
            return (((b[0] & 0xff) << 24));
        }
        if (b.length == 2) {
            return (((b[0] & 0xff) << 24) | ((b[1] & 0xff) << 16));
        }
        if (b.length == 3) {
            return (((b[0] & 0xff) << 24) | ((b[1] & 0xff) << 16) | ((b[2] & 0xff) << 8));
        }

        return (((b[0] & 0xff) << 24) | ((b[1] & 0xff) << 16) | ((b[2] & 0xff) << 8) | (b[3] & 0xff));
    }
//----------------------CHECK FREE MEMORY / CHECK HEAP--------------------------
    static long bufferMem = 0;
    static String freeMem = "";
    static long heap = 0; //heap currently used

    public static String checkHeap() {
        Runtime runtime = Runtime.getRuntime();
        long fM = runtime.freeMemory();

        if ((bufferMem - fM) < 0) {
            freeMem = Long.toString(fM);
            //System.out.println("[FreeMemory-PEAK: "+freeMem + " Bytes.]");

            heap = runtime.totalMemory() - fM;
            //System.out.println("[Heap currently used: "+heap+" Bytes.]");
        }

        bufferMem = fM;

        //return freeMem;
        return Long.toString(heap);
    }
//-------------------------------------------------------------------------------	
}
