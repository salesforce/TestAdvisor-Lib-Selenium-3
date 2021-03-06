/*
 * Copyright (c) 2021, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.cte.listener.selenium;

import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;

/**
 * Records information on a given WebDriver command such as click() or getText().
 * 
 * The {@link com.salesforce.cte.listener.selenium.EventDispatcher} creates such a record before and after each command. The Event object
 * is then passed on to any listeners implementing the {@link com.salesforce.cte.listener.selenium.IEventListener} interface.
 * 
 * The default listener is {@link com.salesforce.cte.listener.selenium.FullListener} which collects all Event objects as they come.
 * 
 * @author gneumann
 * @since 1.0
 */
public class WebDriverEvent {
	public enum Type { BeforeAction, AfterAction, BeforeGather, AfterGather, Exception }
	public enum WebDriverInterface { WebDriver, JavascriptExecutor, Options, ImeHandler, Navigation, TargetLocator, Timeouts, Window, Alert, WebElement, Keyboard, Mouse, TakesScreenshot }
	public enum Cmd {
		// commands called directly from WebDriver object
		close(WebDriverInterface.WebDriver, "close"),
		findElementByWebDriver(WebDriverInterface.WebDriver, "findElement"),
		findElementsByWebDriver(WebDriverInterface.WebDriver, "findElements"),
		get(WebDriverInterface.WebDriver, "get"),
		getCurrentUrl(WebDriverInterface.WebDriver, "getCurrentUrl"),
		getPageSource(WebDriverInterface.WebDriver, "getPageSource"),
		getTitle(WebDriverInterface.WebDriver, "getTitle"),
		getWindowHandle(WebDriverInterface.WebDriver, "getWindowHandle"),
		getWindowHandles(WebDriverInterface.WebDriver, "getWindowHandles"),
		quit(WebDriverInterface.WebDriver, "quit"),
		// commands called directly from WebDriver object after casting to JavascriptExecutor
		executeAsyncScript(WebDriverInterface.JavascriptExecutor, "executeAsyncScript"),
		executeScript(WebDriverInterface.JavascriptExecutor, "executeScript"),
		// commands called directly from WebDriver object after casting to TakesScreenshot
		getScreenshotAs(WebDriverInterface.TakesScreenshot, "getScreenshotAs"),
		// commands called directly from WebDriver.Options object
		addCookie(WebDriverInterface.Options, "addCookie"),
		deleteCookieNamed(WebDriverInterface.Options, "deleteCookieNamed"),
		deleteCookie(WebDriverInterface.Options, "deleteCookie"),
		deleteAllCookies(WebDriverInterface.Options, "deleteAllCookies"),
		getCookies(WebDriverInterface.Options, "getCookies"),
		getCookieNamed(WebDriverInterface.Options, "getCookieNamed"),
		// commands called directly from WebDriver.ImeHandler object
		getAvailableEngines(WebDriverInterface.ImeHandler, "getAvailableEngines"),
		getActiveEngine(WebDriverInterface.ImeHandler, "getActiveEngine"),
		isActivated(WebDriverInterface.ImeHandler, "isActivated"),
		deactivate(WebDriverInterface.ImeHandler, "deactivate"),
		activateEngine(WebDriverInterface.ImeHandler, "activateEngine"),
		// commands called directly from WebDriver.Timeouts object
		implicitlyWait(WebDriverInterface.Timeouts, "implicitlyWait"),
		pageLoadTimeout(WebDriverInterface.Timeouts, "pageLoadTimeout"),
		setScriptTimeout(WebDriverInterface.Timeouts, "setScriptTimeout"),
		// commands called directly from WebDriver.Navigation object
		back(WebDriverInterface.Navigation, "back"),
		forward(WebDriverInterface.Navigation, "forward"),
		refresh(WebDriverInterface.Navigation, "refresh"),
		to(WebDriverInterface.Navigation, "to"),
		// commands called directly from WebDriver.TargetLocator object
		activeElement(WebDriverInterface.TargetLocator, "activeElement"),
		defaultContent(WebDriverInterface.TargetLocator, "defaultContent"),
		frameByIndex(WebDriverInterface.TargetLocator, "frame"),
		frameByElement(WebDriverInterface.TargetLocator, "frame"),
		parentFrame(WebDriverInterface.TargetLocator, "parentFrame"),
		window(WebDriverInterface.TargetLocator, "window"),
		// commands called directly from WebDriver.Window object
		fullscreen(WebDriverInterface.Window, "fullscreen"),
		getPosition(WebDriverInterface.Window, "getPosition"),
		getSizeByWindow(WebDriverInterface.Window, "getSize"),
		maximize(WebDriverInterface.Window, "maximize"),
		setPosition(WebDriverInterface.Window, "setPosition"),
		setSizeByWindow(WebDriverInterface.Window, "setSize"),
		// commands called directly from Alert object
		dismiss(WebDriverInterface.Window, "dismiss"),
		accept(WebDriverInterface.Window, "accept"),
		getTextByAlert(WebDriverInterface.Window, "getText"),
		sendKeysByAlert(WebDriverInterface.Window, "sendKeys"),
		// commands called directly from WebElement object
		clickByElement(WebDriverInterface.WebElement, "click"),
		clear(WebDriverInterface.WebElement, "clear"),
		findElementByElement(WebDriverInterface.WebElement, "findElement"),
		findElementsByElement(WebDriverInterface.WebElement, "findElements"),
		getAttribute(WebDriverInterface.WebElement, "getAttribute"),
		getCoordinates(WebDriverInterface.WebElement, "getCoordinates"),
		getCssValue(WebDriverInterface.WebElement, "getCssValue"),
		getScreenshotAsByElement(WebDriverInterface.WebElement, "getScreenshotAs"),
		getTagName(WebDriverInterface.WebElement, "getTagName"),
		getText(WebDriverInterface.WebElement, "getText"),
		isDisplayed(WebDriverInterface.WebElement, "isDisplayed"),
		isEnabled(WebDriverInterface.WebElement, "isEnabled"),
		isSelected(WebDriverInterface.WebElement, "isSelected"),
		getLocation(WebDriverInterface.WebElement, "getLocation"),
		getSizeByElement(WebDriverInterface.WebElement, "getSize"),
		getRect(WebDriverInterface.WebElement, "getRect"),
		sendKeysByElement(WebDriverInterface.WebElement, "sendKeys"),
		uploadFile(WebDriverInterface.WebElement, "sendKeys"),
		submit(WebDriverInterface.WebElement, "submit"),
		// commands called directly from Keyboard object
		sendKeysByKeyboard(WebDriverInterface.Keyboard, "sendKeys"),
		pressKey(WebDriverInterface.Keyboard, "pressKey"),
		releaseKey(WebDriverInterface.Keyboard, "releaseKey"),
		// commands called directly from Mouse object
		clickByMouse(WebDriverInterface.Mouse, "click"),
		doubleClick(WebDriverInterface.Mouse, "doubleClick"),
		mouseDown(WebDriverInterface.Mouse, "mouseDown"),
		mouseUp(WebDriverInterface.Mouse, "mouseUp"),
		mouseMove(WebDriverInterface.Mouse, "mouseMove"),
		mouseMoveWithOffset(WebDriverInterface.Mouse, "mouseMove"),
		contextClick(WebDriverInterface.Mouse, "contextClick");

		Cmd(WebDriverInterface wdIf, String shortCmdString) {
			this.wdIf = wdIf;
			this.shortCmdString = shortCmdString;
		}

		private final WebDriverInterface wdIf;
		private final String shortCmdString;

		public String getShortCmdString() {
			return this.shortCmdString;
		}

		public String getLongCmdString() {
			return (this.wdIf == WebDriverInterface.WebElement)
					? getLongCmdString("webElement")
					: getLongCmdString("webDriver");
		}

		public String getLongCmdString(String fieldName) {
			String value = null;
			String shortCmd = getShortCmdString();
			switch(this.wdIf) {
			case WebDriver:
				value = fieldName + "." + shortCmd;
				break;
			case JavascriptExecutor:
				value = "(JavascriptExecutor) " + fieldName + "." + shortCmd;
				break;
			case TakesScreenshot:
				value = "(TakesScreenshot) " + fieldName + "." + shortCmd;
				break;
			case Options:
				value = fieldName + ".manage()." + shortCmd;
				break;
			case ImeHandler:
				value = fieldName + ".manage().ime()." + shortCmd;
				break;
			case Navigation:
				value = fieldName + ".navigate()." + shortCmd;
				break;
			case TargetLocator:
				value = fieldName + ".switchTo()." + shortCmd;
				break;
			case Timeouts:
				value = fieldName + ".manage().timeouts()." + shortCmd;
				break;
			case Window:
				value = fieldName + ".manage().window()." + shortCmd;
				break;
			case Alert:
				value = fieldName + ".switchTo().alert()." + shortCmd;
				break;
			case WebElement:
				value = fieldName + "." + shortCmd;
				break;
			case Keyboard:
				value = fieldName + ".getKeyboard()." + shortCmd;
				break;
			case Mouse:
				value = fieldName + ".getMouse()." + shortCmd;
				break;
			}

			return value;
		}
	}

	private static long timeMarkerElapsedAction;
	private static long timeMarkerSinceLastEvent;
	private static int lastRecordNumber = 1;

	private int recordNumber = -1;
	private int eventNumber = -1;
	private long timeStamp = -1L; // System.currentTimeMillis()
	private long timeSinceLastAction = -1L; // measured from end of last action to begin of current action
	private long timeElapsedEvent = -1L; // measured from begin of current command to end of current command
	private Type typeOfLog;
	private Cmd cmd;
	private String param1;
	private String param2;
	private String returnValue;
	@JsonIgnore
	private Object returnObject;
	@JsonProperty(access = Access.READ_ONLY)
	private Throwable issue;
	private String elementLocator;

	/**
	 * Empty Default constructor to be used by de-serialization.
	 */
	public WebDriverEvent( ) {
		// no-op
	}

	public WebDriverEvent(Type typeOfLog, int eventNumber, Cmd cmd) {
		this.recordNumber = WebDriverEvent.lastRecordNumber++;
		this.typeOfLog = typeOfLog;
		this.eventNumber = eventNumber;
		this.cmd = cmd;
		this.timeStamp = System.currentTimeMillis();
		
		switch(typeOfLog) {
		case BeforeAction:
			timeStampsForBeginAction();
			timeStampsForBeginEvent();
			break;
		case AfterAction:
			timeStampsForAfterAction();
			timeStampsForAfterEvent();
			break;
		case BeforeGather:
			timeStampsForBeginEvent();
			break;
		case AfterGather:
			timeStampsForAfterEvent();
			break;
		default:
		}
	}

	public int getRecordNumber() {
		return recordNumber;
	}

	public int getEventNumber() {
		return eventNumber;
	}

	public long getTimeStamp() {
		return timeStamp;
	}

	public long getTimeSinceLastAction() {
		return timeSinceLastAction;
	}

	public long getTimeElapsedEvent() {
		return timeElapsedEvent;
	}

	public Type getTypeOfLog() {
		return typeOfLog;
	}

	public Cmd getCmd() {
		return cmd;
	}

	public String getParam1() {
		return param1;
	}

	public void setParam1(String param1) {
		this.param1 = param1;
	}

	public String getParam2() {
		return param2;
	}

	public void setParam2(String param2) {
		this.param2 = param2;
	}

	public String getReturnValue() {
		return returnValue;
	}

	public void setReturnValue(String returnValue) {
		this.returnValue = returnValue;
	}

	public Object getReturnObject() {
		return returnObject;
	}

	public void setReturnObject(Object returnObject) {
		this.returnObject = returnObject;
	}

	public Throwable getIssue() {
		return issue;
	}

	public void setIssue(Throwable issue) {
		this.issue = issue;
	}

	public String getElementLocator() {
		return elementLocator;
	}

	public void setElementLocator(String elementLocator) {
		this.elementLocator = elementLocator;
	}

	@Override
	public String toString() {
		StringBuilder buffer = new StringBuilder();
		buffer.append("eventno:").append(eventNumber).append(",");
		buffer.append("type:").append(typeOfLog).append(",");
		buffer.append("timestamp:").append(timeStamp).append(" ms,");
		buffer.append("cmd:").append(cmd);
		if (param1 != null) {
			buffer.append(",").append("param1:").append(param1);
		}
		if (param2 != null) {
			buffer.append(",").append("param2:").append(param2);
		}
		if (returnValue != null) {
			buffer.append(",").append("returned:").append(returnValue).append(",");
		}
		if (returnObject != null) {
			buffer.append(",").append("returned:").append(returnObject.toString()).append(",");
		}
		if (timeSinceLastAction != -1L) {
			buffer.append(",").append("since last action:").append(formattedNanoTime(timeSinceLastAction));
		}
		if (timeElapsedEvent != -1L) {
			buffer.append(",").append("executed in:").append(formattedNanoTime(timeElapsedEvent));
		}
		if (issue != null) {
			buffer.append(",").append("issue:").append(issue.getMessage());
		}
		
		return buffer.toString();
	}

	public static String formattedNanoTime(long duration) {
		return String.format("%d sec %d ms", TimeUnit.NANOSECONDS.toSeconds(duration),
				TimeUnit.NANOSECONDS.toMillis(duration)
						- TimeUnit.SECONDS.toMillis(TimeUnit.NANOSECONDS.toSeconds(duration)));
	}

	private void timeStampsForBeginAction() {
		if (eventNumber > 1) {
			timeSinceLastAction = System.nanoTime() - timeMarkerSinceLastEvent;
		}
	}

	private void timeStampsForAfterAction() {
		timeMarkerSinceLastEvent = System.nanoTime();
	}

	private void timeStampsForBeginEvent() {
		timeMarkerElapsedAction = System.nanoTime();
	}

	private void timeStampsForAfterEvent() {
		timeElapsedEvent = System.nanoTime() - timeMarkerElapsedAction;
	}

	public static String getLocatorFromWebElement(WebElement elem) {
		return (elem != null) ? getLocatorFromWebElement(elem.toString()) : null;
	}

	public static String getLocatorFromWebElement(String locator) {
		if (locator == null)
			return null;

		// sample string:
		// "[[RemoteWebDriver: firefox on WINDOWS (a66f78e9668e4aa3b066239459f969fe)] -> xpath: .//*[@id='Country__c_body']/table/tbody/tr[2]/th/a]"
		Pattern outerPattern = Pattern.compile("(\\[\\[.+\\] -> )(.+)\\]");
		Matcher outerMatcher = outerPattern.matcher(locator);
		if (!outerMatcher.matches()) {
			// return toString() as-is
			return locator;
		}

		// try to get the locator
		locator = locator.substring(outerMatcher.start(2), outerMatcher.end(2));
		// sample string:
		// "xpath: .//*[@id='Country__c_body']/table/tbody/tr[2]/th/a]"
		Pattern innerPattern = Pattern.compile("(\\S+): (.+)");
		Matcher innerMatcher = innerPattern.matcher(locator);
		boolean isLinkText = false;
		if (!innerMatcher.matches()) {
			innerPattern = Pattern.compile("(link text): (.+)");
			innerMatcher = innerPattern.matcher(locator);
			if (innerMatcher.matches()) {
				isLinkText = true;
			} else {
				// return what we got with the outer matcher
				return locator;
			}
		}

		// build the @FindBy string
		StringBuilder sb = new StringBuilder();
		sb.append("By.");
		// append locator type: "xpath"
		String locatorType = (isLinkText) ? "linkText" : locator.substring(innerMatcher.start(1), innerMatcher.end(1));
		sb.append(locatorType).append("(\"");
		// append locator itself: ".//*[@id='Country__c_body']/table/tbody/tr[2]/th/a]"
		sb.append(locator.substring(innerMatcher.start(2), innerMatcher.end(2))).append("\")");
		return sb.toString();
	}

	/**
	 * Retrieve the locator information from By's toString() representation.
	 * @param by instance of By
	 * @return locator defined in By object
	 */
	public static String getLocatorFromBy(By by) {
		return (by != null) ? getLocatorFromBy(by.toString()) : null;
	}

	/**
	 * Retrieve the locator information from By's toString() representation.
	 * @param locator toString() representation of a By object
	 * @return locator defined in By object
	 */
	public static String getLocatorFromBy(String locator) {
		if (locator == null)
			return null;
		// sample string:
		// "By.xpath: .//*[@id='thePage:j_id39:searchblock:test:j_id45_lkwgt']/img"
		Pattern pattern = Pattern.compile("By.(\\S+): (.+)");
		Matcher matcher = pattern.matcher(locator);
		if (!matcher.matches()) {
			// return what we got as-is
			return locator;
		}

		// build the @FindBy string
		StringBuilder sb = new StringBuilder();
		sb.append("By.");
		// append locator type: "xpath"
		sb.append(locator.substring(matcher.start(1), matcher.end(1))).append("(\"");
		// append locator itself:
		// ".//*[@id='thePage:j_id39:searchblock:test:j_id45_lkwgt']/img"
		sb.append(locator.substring(matcher.start(2), matcher.end(2))).append("\")");
		return sb.toString();
	}
	
	/**
	 * Converts a given string into the appropriate By object.
	 * @param param string containing a locator
	 * @return By object or null in case parsing fails
	 */
	public static By getByFromString(String param) {
		By locator = null;
		if (param.startsWith("By.xpath")) {
			locator = By.xpath(param.substring("By.xpath".length() + 2, param.length() - 2));
		} else if (param.startsWith("By.cssSelector")) {
			locator = By.cssSelector(param.substring("By.cssSelector".length() + 2, param.length() - 2));
		} else if (param.startsWith("css selector")) {
			locator = By.cssSelector(param.substring("css selector".length() + 2));
		} else if (param.startsWith("By.id")) {
			locator = By.id(param.substring("By.id".length() + 2, param.length() - 2));
		} else if (param.startsWith("By.name")) {
			locator = By.name(param.substring("By.name".length() + 2, param.length() - 2));
		} else if (param.startsWith("By.tagName")) {
			locator = By.tagName(param.substring("By.tagName".length() + 2, param.length() - 2));
		} else if (param.startsWith("By.className")) {
			locator = By.className(param.substring("By.className".length() + 2, param.length() - 2));
		} else if (param.startsWith("By.linkText")) {
			locator = By.linkText(param.substring("By.linkText".length() + 2, param.length() - 2));
		} else if (param.startsWith("By.partialLinkText")) {
			locator = By.partialLinkText(param.substring("By.partialLinkText".length() + 2, param.length() - 2));
		} else {
			System.err.print("Problem converting param into By: " + param);
		}
		return locator;
	}
}
