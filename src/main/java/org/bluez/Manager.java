package org.bluez;
import java.util.List;
import java.util.Map;
import org.freedesktop.dbus.DBusInterface;
import org.freedesktop.dbus.DBusSignal;
import org.freedesktop.dbus.Variant;
import org.freedesktop.dbus.exceptions.DBusException;
public interface Manager extends DBusInterface
{
   public static class PropertyChanged extends DBusSignal
   {
      public final String a;
      public final Variant b;
      public PropertyChanged(String path, String a, Variant b) throws DBusException
      {
         super(path, a, b);
         this.a = a;
         this.b = b;
      }
   }
   public static class AdapterAdded extends DBusSignal
   {
      public final DBusInterface a;
      public AdapterAdded(String path, DBusInterface a) throws DBusException
      {
         super(path, a);
         this.a = a;
      }
   }
   public static class AdapterRemoved extends DBusSignal
   {
      public final DBusInterface a;
      public AdapterRemoved(String path, DBusInterface a) throws DBusException
      {
         super(path, a);
         this.a = a;
      }
   }
   public static class DefaultAdapterChanged extends DBusSignal
   {
      public final DBusInterface a;
      public DefaultAdapterChanged(String path, DBusInterface a) throws DBusException
      {
         super(path, a);
         this.a = a;
      }
   }

  public Map<String,Variant> GetProperties();
  public DBusInterface DefaultAdapter();
  public DBusInterface FindAdapter(String a);
  public List<DBusInterface> ListAdapters();

}
