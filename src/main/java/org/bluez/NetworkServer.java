package org.bluez;
import org.freedesktop.dbus.DBusInterface;
public interface NetworkServer extends DBusInterface
{

  public void Register(String a, String b);
  public void Unregister(String a);

}
