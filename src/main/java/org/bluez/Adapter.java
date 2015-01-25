package org.bluez;
import java.util.List;
import java.util.Map;
import org.freedesktop.dbus.DBusInterface;
import org.freedesktop.dbus.DBusSignal;
import org.freedesktop.dbus.Variant;
import org.freedesktop.dbus.exceptions.DBusException;
public interface Adapter extends DBusInterface
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
   public static class DeviceCreated extends DBusSignal
   {
      public final DBusInterface a;
      public DeviceCreated(String path, DBusInterface a) throws DBusException
      {
         super(path, a);
         this.a = a;
      }
   }
   public static class DeviceRemoved extends DBusSignal
   {
      public final DBusInterface a;
      public DeviceRemoved(String path, DBusInterface a) throws DBusException
      {
         super(path, a);
         this.a = a;
      }
   }
   public static class DeviceFound extends DBusSignal
   {
      public final String a;
      public final Map<String,Variant> b;
      public DeviceFound(String path, String a, Map<String,Variant> b) throws DBusException
      {
         super(path, a, b);
         this.a = a;
         this.b = b;
      }
   }
   public static class DeviceDisappeared extends DBusSignal
   {
      public final String a;
      public DeviceDisappeared(String path, String a) throws DBusException
      {
         super(path, a);
         this.a = a;
      }
   }

  public Map<String,Variant> GetProperties();
  public void SetProperty(String a, Variant b);
  public void RequestSession();
  public void ReleaseSession();
  public void StartDiscovery();
  public void StopDiscovery();
  public List<DBusInterface> ListDevices();
  public DBusInterface CreateDevice(String a);
  public DBusInterface CreatePairedDevice(String a, DBusInterface b, String c);
  public void CancelDeviceCreation(String a);
  public void RemoveDevice(DBusInterface a);
  public DBusInterface FindDevice(String a);
  public void RegisterAgent(DBusInterface a, String b);
  public void UnregisterAgent(DBusInterface a);

}
