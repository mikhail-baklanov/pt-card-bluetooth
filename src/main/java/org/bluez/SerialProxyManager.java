package org.bluez;
import java.util.List;
import org.freedesktop.dbus.DBusInterface;
import org.freedesktop.dbus.DBusSignal;
import org.freedesktop.dbus.exceptions.DBusException;
public interface SerialProxyManager extends DBusInterface
{
   public static class ProxyCreated extends DBusSignal
   {
      public final String a;
      public ProxyCreated(String path, String a) throws DBusException
      {
         super(path, a);
         this.a = a;
      }
   }
   public static class ProxyRemoved extends DBusSignal
   {
      public final String a;
      public ProxyRemoved(String path, String a) throws DBusException
      {
         super(path, a);
         this.a = a;
      }
   }

  public String CreateProxy(String a, String b);
  public List<String> ListProxies();
  public void RemoveProxy(String a);

}
