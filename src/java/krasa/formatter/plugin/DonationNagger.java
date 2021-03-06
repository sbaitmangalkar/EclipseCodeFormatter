package krasa.formatter.plugin;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

import javax.swing.event.HyperlinkEvent;

import org.jetbrains.annotations.NotNull;

import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.PluginManager;
import com.intellij.notification.*;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.extensions.PluginId;

public class DonationNagger {
	private static final Logger LOG = Logger.getInstance(DonationNagger.class);

	public static final NotificationGroup NOTIFICATION = new NotificationGroup("Eclipse Code Formatter donation", NotificationDisplayType.STICKY_BALLOON, true);

	public static final String DONATE = "Click <a href=\"https://www.paypal.com/cgi-bin/webscr?cmd=_donations&business=75YN7U7H7D7XU&lc=CZ&item_name=Eclipse%20Code%20Formatter%20%2d%20IntelliJ%20plugin%20%2d%20Donation&currency_code=USD&bn=PP%2dDonationsBF%3abtn_donateCC_LG%2egif%3aNonHostedGuest\">"
			+ "here</a> if you would like to make a donation via PayPal.";
	public static final String TITLE = "Thank you for using Eclipse Code Formatter";

	private long actionsExecuted;
	private Date firstUsage;
	private Date lastNaggingDate;
	private Date lastDonationDate;
	private String firstUsedVersion;

	public long getActionsExecuted() {
		return actionsExecuted;
	}

	public void setActionsExecuted(long actionsExecuted) {
		this.actionsExecuted = actionsExecuted;
	}

	public Date getLastNaggingDate() {
		return lastNaggingDate;
	}

	public void setLastNaggingDate(Date lastNaggingDate) {
		this.lastNaggingDate = lastNaggingDate;
	}

	public Date getLastDonationDate() {
		return lastDonationDate;
	}

	public void setLastDonationDate(Date lastDonationDate) {
		this.lastDonationDate = lastDonationDate;
	}

	public Date getFirstUsage() {
		return firstUsage;
	}

	public void setFirstUsage(Date firstUsage) {
		this.firstUsage = firstUsage;
	}

	public String getFirstUsedVersion() {
		return firstUsedVersion;
	}

	public void setFirstUsedVersion(String firstUsedVersion) {
		this.firstUsedVersion = firstUsedVersion;
	}

	public void actionExecuted() {
		try {
			actionsExecuted++;

			if (firstUsage == null) {
				firstUsage = new Date();
			}

			if (firstUsedVersion == null) {
				IdeaPluginDescriptor plugin = PluginManager.getPlugin(PluginId.getId("EclipseCodeFormatter"));
				if (plugin != null) {
					firstUsedVersion = plugin.getVersion();
				}
			}

			if (notDonatedRecently() && notNaggedRecently()) {

				if (actionsExecuted == 10 && probablyNotNewUser()) {

					nag(DONATE);
				} else if (actionsExecuted % 1000 == 0) {
					nag(DONATE);
				}
			}
		} catch (Exception e) {
			LOG.error(e);
		}
	}

	@NotNull
	private void nag(String content) {
		lastNaggingDate = new Date();

		Notification notification = NOTIFICATION.createNotification(TITLE, content, NotificationType.INFORMATION,
				new NotificationListener.UrlOpeningListener(true) {
					@Override
					protected void hyperlinkActivated(@NotNull Notification notification1, @NotNull HyperlinkEvent event) {
						super.hyperlinkActivated(notification1, event);
						nagged();
					}
				});

		ApplicationManager.getApplication().invokeLater(() -> Notifications.Bus.notify(notification));
	}

	private boolean probablyNotNewUser() {
		LocalDate firstUse = firstUsage.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
		LocalDate today = LocalDate.now();

		return today.isAfter(firstUse.plusMonths(1)); // have it for more than month

	}

	private boolean notDonatedRecently() {
		if (lastDonationDate == null) {
			return true;
		}
		LocalDate today = LocalDate.now();
		LocalDate lastNag = lastDonationDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
		return lastNag.isBefore(today.minusMonths(12));
	}

	private boolean notNaggedRecently() {
		if (lastNaggingDate == null) {
			return true;
		}
		LocalDate today = LocalDate.now();
		LocalDate lastNag = lastNaggingDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
		return lastNag.isBefore(today.minusMonths(3));
	}

	private void nagged() {
		lastDonationDate = new Date();
	}

}
