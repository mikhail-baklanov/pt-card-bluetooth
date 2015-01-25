package org.bluez;
import org.freedesktop.dbus.DBusInterface;
import org.freedesktop.dbus.UInt32;
public interface Service extends DBusInterface
{

  public UInt32 AddRecord(String a);
  public void UpdateRecord(UInt32 a, String b);
  public void RemoveRecord(UInt32 a);
  public void RequestAuthorization(String a, UInt32 b);
  public void CancelAuthorization();

}
