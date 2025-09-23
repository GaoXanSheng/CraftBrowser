/**
 * The MIT License
 * Copyright © 2017 WebFolder OÜ
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package io.webfolder.cdp.event;

import io.webfolder.cdp.event.animation.AnimationCanceled;
import io.webfolder.cdp.event.animation.AnimationCreated;
import io.webfolder.cdp.event.animation.AnimationStarted;
import io.webfolder.cdp.event.applicationcache.ApplicationCacheStatusUpdated;
import io.webfolder.cdp.event.applicationcache.NetworkStateUpdated;
import io.webfolder.cdp.event.console.MessageAdded;
import io.webfolder.cdp.event.css.*;
import io.webfolder.cdp.event.database.AddDatabase;
import io.webfolder.cdp.event.debugger.*;
import io.webfolder.cdp.event.dom.*;
import io.webfolder.cdp.event.domstorage.DomStorageItemAdded;
import io.webfolder.cdp.event.domstorage.DomStorageItemRemoved;
import io.webfolder.cdp.event.domstorage.DomStorageItemUpdated;
import io.webfolder.cdp.event.domstorage.DomStorageItemsCleared;
import io.webfolder.cdp.event.emulation.VirtualTimeBudgetExpired;
import io.webfolder.cdp.event.heapprofiler.*;
import io.webfolder.cdp.event.inspector.Detached;
import io.webfolder.cdp.event.inspector.TargetCrashed;
import io.webfolder.cdp.event.layertree.LayerPainted;
import io.webfolder.cdp.event.layertree.LayerTreeDidChange;
import io.webfolder.cdp.event.log.EntryAdded;
import io.webfolder.cdp.event.network.*;
import io.webfolder.cdp.event.overlay.InspectNodeRequested;
import io.webfolder.cdp.event.overlay.NodeHighlightRequested;
import io.webfolder.cdp.event.page.*;
import io.webfolder.cdp.event.profiler.ConsoleProfileFinished;
import io.webfolder.cdp.event.profiler.ConsoleProfileStarted;
import io.webfolder.cdp.event.runtime.*;
import io.webfolder.cdp.event.security.CertificateError;
import io.webfolder.cdp.event.security.SecurityStateChanged;
import io.webfolder.cdp.event.serviceworker.WorkerErrorReported;
import io.webfolder.cdp.event.serviceworker.WorkerRegistrationUpdated;
import io.webfolder.cdp.event.serviceworker.WorkerVersionUpdated;
import io.webfolder.cdp.event.target.*;
import io.webfolder.cdp.event.tethering.Accepted;
import io.webfolder.cdp.event.tracing.BufferUsage;
import io.webfolder.cdp.event.tracing.DataCollected;
import io.webfolder.cdp.event.tracing.TracingComplete;

public enum Events {
  /**
   * Fired when remote debugging connection is about to be terminated
   * Contains detach reason
   */
  InspectorDetached("Inspector", "detached", Detached.class),

  /**
   * Fired when debugging target has crashed
   */
  InspectorTargetCrashed("Inspector", "targetCrashed", TargetCrashed.class),

  PageDomContentEventFired("Page", "domContentEventFired", DomContentEventFired.class),

  PageLoadEventFired("Page", "loadEventFired", LoadEventFired.class),

  /**
   * Fired when frame has been attached to its parent
   */
  PageFrameAttached("Page", "frameAttached", FrameAttached.class),

  /**
   * Fired once navigation of the frame has completed
   * Frame is now associated with the new loader
   */
  PageFrameNavigated("Page", "frameNavigated", FrameNavigated.class),

  /**
   * Fired when frame has been detached from its parent
   */
  PageFrameDetached("Page", "frameDetached", FrameDetached.class),

  /**
   * Fired when frame has started loading
   */
  PageFrameStartedLoading("Page", "frameStartedLoading", FrameStartedLoading.class),

  /**
   * Fired when frame has stopped loading
   */
  PageFrameStoppedLoading("Page", "frameStoppedLoading", FrameStoppedLoading.class),

  /**
   * Fired when frame schedules a potential navigation
   */
  PageFrameScheduledNavigation("Page", "frameScheduledNavigation", FrameScheduledNavigation.class),

  /**
   * Fired when frame no longer has a scheduled navigation
   */
  PageFrameClearedScheduledNavigation("Page", "frameClearedScheduledNavigation", FrameClearedScheduledNavigation.class),

  PageFrameResized("Page", "frameResized", FrameResized.class),

  /**
   * Fired when a JavaScript initiated dialog (alert, confirm, prompt, or onbeforeunload) is about to open
   */
  PageJavascriptDialogOpening("Page", "javascriptDialogOpening", JavascriptDialogOpening.class),

  /**
   * Fired when a JavaScript initiated dialog (alert, confirm, prompt, or onbeforeunload) has been closed
   */
  PageJavascriptDialogClosed("Page", "javascriptDialogClosed", JavascriptDialogClosed.class),

  /**
   * Compressed image data requested by the <code>startScreencast</code>
   */
  PageScreencastFrame("Page", "screencastFrame", ScreencastFrame.class),

  /**
   * Fired when the page with currently enabled screencast was shown or hidden </code>
   */
  PageScreencastVisibilityChanged("Page", "screencastVisibilityChanged", ScreencastVisibilityChanged.class),

  /**
   * Fired when interstitial page was shown
   */
  PageInterstitialShown("Page", "interstitialShown", InterstitialShown.class),

  /**
   * Fired when interstitial page was hidden
   */
  PageInterstitialHidden("Page", "interstitialHidden", InterstitialHidden.class),

  /**
   * Fired when a navigation is started if navigation throttles are enabled
   * The navigation will be deferred until processNavigation is called
   */
  PageNavigationRequested("Page", "navigationRequested", NavigationRequested.class),

  /**
   * Fired when the node should be highlighted
   * This happens after call to <code>setInspectMode</code>
   */
  OverlayNodeHighlightRequested("Overlay", "nodeHighlightRequested", NodeHighlightRequested.class),

  /**
   * Fired when the node should be inspected
   * This happens after call to <code>setInspectMode</code> or when user manually inspects an element
   */
  OverlayInspectNodeRequested("Overlay", "inspectNodeRequested", InspectNodeRequested.class),

  /**
   * Notification sent after the virual time budget for the current VirtualTimePolicy has run out
   */
  EmulationVirtualTimeBudgetExpired("Emulation", "virtualTimeBudgetExpired", VirtualTimeBudgetExpired.class),

  /**
   * The security state of the page changed
   */
  SecuritySecurityStateChanged("Security", "securityStateChanged", SecurityStateChanged.class),

  /**
   * There is a certificate error
   * If overriding certificate errors is enabled, then it should be handled with the handleCertificateError command
   * Note: this event does not fire if the certificate error has been allowed internally
   */
  SecurityCertificateError("Security", "certificateError", CertificateError.class),

  /**
   * Fired when resource loading priority is changed
   */
  NetworkResourceChangedPriority("Network", "resourceChangedPriority", ResourceChangedPriority.class),

  /**
   * Fired when page is about to send HTTP request
   */
  NetworkRequestWillBeSent("Network", "requestWillBeSent", RequestWillBeSent.class),

  /**
   * Fired if request ended up loading from cache
   */
  NetworkRequestServedFromCache("Network", "requestServedFromCache", RequestServedFromCache.class),

  /**
   * Fired when HTTP response is available
   */
  NetworkResponseReceived("Network", "responseReceived", ResponseReceived.class),

  /**
   * Fired when data chunk was received over the network
   */
  NetworkDataReceived("Network", "dataReceived", DataReceived.class),

  /**
   * Fired when HTTP request has finished loading
   */
  NetworkLoadingFinished("Network", "loadingFinished", LoadingFinished.class),

  /**
   * Fired when HTTP request has failed to load
   */
  NetworkLoadingFailed("Network", "loadingFailed", LoadingFailed.class),

  /**
   * Fired when WebSocket is about to initiate handshake
   */
  NetworkWebSocketWillSendHandshakeRequest("Network", "webSocketWillSendHandshakeRequest", WebSocketWillSendHandshakeRequest.class),

  /**
   * Fired when WebSocket handshake response becomes available
   */
  NetworkWebSocketHandshakeResponseReceived("Network", "webSocketHandshakeResponseReceived", WebSocketHandshakeResponseReceived.class),

  /**
   * Fired upon WebSocket creation
   */
  NetworkWebSocketCreated("Network", "webSocketCreated", WebSocketCreated.class),

  /**
   * Fired when WebSocket is closed
   */
  NetworkWebSocketClosed("Network", "webSocketClosed", WebSocketClosed.class),

  /**
   * Fired when WebSocket frame is received
   */
  NetworkWebSocketFrameReceived("Network", "webSocketFrameReceived", WebSocketFrameReceived.class),

  /**
   * Fired when WebSocket frame error occurs
   */
  NetworkWebSocketFrameError("Network", "webSocketFrameError", WebSocketFrameError.class),

  /**
   * Fired when WebSocket frame is sent
   */
  NetworkWebSocketFrameSent("Network", "webSocketFrameSent", WebSocketFrameSent.class),

  /**
   * Fired when EventSource message is received
   */
  NetworkEventSourceMessageReceived("Network", "eventSourceMessageReceived", EventSourceMessageReceived.class),

  DatabaseAddDatabase("Database", "addDatabase", AddDatabase.class),

  DOMStorageDomStorageItemsCleared("DOMStorage", "domStorageItemsCleared", DomStorageItemsCleared.class),

  DOMStorageDomStorageItemRemoved("DOMStorage", "domStorageItemRemoved", DomStorageItemRemoved.class),

  DOMStorageDomStorageItemAdded("DOMStorage", "domStorageItemAdded", DomStorageItemAdded.class),

  DOMStorageDomStorageItemUpdated("DOMStorage", "domStorageItemUpdated", DomStorageItemUpdated.class),

  ApplicationCacheApplicationCacheStatusUpdated("ApplicationCache", "applicationCacheStatusUpdated", ApplicationCacheStatusUpdated.class),

  ApplicationCacheNetworkStateUpdated("ApplicationCache", "networkStateUpdated", NetworkStateUpdated.class),

  /**
   * Fired when <code>Document</code> has been totally updated
   * Node ids are no longer valid
   */
  DOMDocumentUpdated("DOM", "documentUpdated", DocumentUpdated.class),

  /**
   * Fired when backend wants to provide client with the missing DOM structure
   * This happens upon most of the calls requesting node ids
   */
  DOMSetChildNodes("DOM", "setChildNodes", SetChildNodes.class),

  /**
   * Fired when <code>Element</code>'s attribute is modified
   */
  DOMAttributeModified("DOM", "attributeModified", AttributeModified.class),

  /**
   * Fired when <code>Element</code>'s attribute is removed
   */
  DOMAttributeRemoved("DOM", "attributeRemoved", AttributeRemoved.class),

  /**
   * Fired when <code>Element</code>'s inline style is modified via a CSS property modification
   */
  DOMInlineStyleInvalidated("DOM", "inlineStyleInvalidated", InlineStyleInvalidated.class),

  /**
   * Mirrors <code>DOMCharacterDataModified</code> event
   */
  DOMCharacterDataModified("DOM", "characterDataModified", CharacterDataModified.class),

  /**
   * Fired when <code>Container</code>'s child node count has changed
   */
  DOMChildNodeCountUpdated("DOM", "childNodeCountUpdated", ChildNodeCountUpdated.class),

  /**
   * Mirrors <code>DOMNodeInserted</code> event
   */
  DOMChildNodeInserted("DOM", "childNodeInserted", ChildNodeInserted.class),

  /**
   * Mirrors <code>DOMNodeRemoved</code> event
   */
  DOMChildNodeRemoved("DOM", "childNodeRemoved", ChildNodeRemoved.class),

  /**
   * Called when shadow root is pushed into the element
   */
  DOMShadowRootPushed("DOM", "shadowRootPushed", ShadowRootPushed.class),

  /**
   * Called when shadow root is popped from the element
   */
  DOMShadowRootPopped("DOM", "shadowRootPopped", ShadowRootPopped.class),

  /**
   * Called when a pseudo element is added to an element
   */
  DOMPseudoElementAdded("DOM", "pseudoElementAdded", PseudoElementAdded.class),

  /**
   * Called when a pseudo element is removed from an element
   */
  DOMPseudoElementRemoved("DOM", "pseudoElementRemoved", PseudoElementRemoved.class),

  /**
   * Called when distrubution is changed
   */
  DOMDistributedNodesUpdated("DOM", "distributedNodesUpdated", DistributedNodesUpdated.class),

  /**
   * Fires whenever a MediaQuery result changes (for example, after a browser window has been resized
   * ) The current implementation considers only viewport-dependent media features
   */
  CSSMediaQueryResultChanged("CSS", "mediaQueryResultChanged", MediaQueryResultChanged.class),

  /**
   * Fires whenever a web font gets loaded
   */
  CSSFontsUpdated("CSS", "fontsUpdated", FontsUpdated.class),

  /**
   * Fired whenever a stylesheet is changed as a result of the client operation
   */
  CSSStyleSheetChanged("CSS", "styleSheetChanged", StyleSheetChanged.class),

  /**
   * Fired whenever an active document stylesheet is added
   */
  CSSStyleSheetAdded("CSS", "styleSheetAdded", StyleSheetAdded.class),

  /**
   * Fired whenever an active document stylesheet is removed
   */
  CSSStyleSheetRemoved("CSS", "styleSheetRemoved", StyleSheetRemoved.class),

  /**
   * Issued when a possible inspection target is created
   */
  TargetTargetCreated("Target", "targetCreated", TargetCreated.class),

  /**
   * Issued when a target is destroyed
   */
  TargetTargetDestroyed("Target", "targetDestroyed", TargetDestroyed.class),

  /**
   * Issued when attached to target because of auto-attach or <code>attachToTarget</code> command
   */
  TargetAttachedToTarget("Target", "attachedToTarget", AttachedToTarget.class),

  /**
   * Issued when detached from target for any reason (including <code>detachFromTarget</code> command)
   */
  TargetDetachedFromTarget("Target", "detachedFromTarget", DetachedFromTarget.class),

  /**
   * Notifies about new protocol message from attached target
   */
  TargetReceivedMessageFromTarget("Target", "receivedMessageFromTarget", ReceivedMessageFromTarget.class),

  ServiceWorkerWorkerRegistrationUpdated("ServiceWorker", "workerRegistrationUpdated", WorkerRegistrationUpdated.class),

  ServiceWorkerWorkerVersionUpdated("ServiceWorker", "workerVersionUpdated", WorkerVersionUpdated.class),

  ServiceWorkerWorkerErrorReported("ServiceWorker", "workerErrorReported", WorkerErrorReported.class),

  LayerTreeLayerTreeDidChange("LayerTree", "layerTreeDidChange", LayerTreeDidChange.class),

  LayerTreeLayerPainted("LayerTree", "layerPainted", LayerPainted.class),

  /**
   * Contains an bucket of collected trace events
   * When tracing is stopped collected events will be send as a sequence of dataCollected events followed by tracingComplete event
   */
  TracingDataCollected("Tracing", "dataCollected", DataCollected.class),

  /**
   * Signals that tracing is stopped and there is no trace buffers pending flush, all data were delivered via dataCollected events
   */
  TracingTracingComplete("Tracing", "tracingComplete", TracingComplete.class),

  TracingBufferUsage("Tracing", "bufferUsage", BufferUsage.class),

  /**
   * Event for each animation that has been created
   */
  AnimationAnimationCreated("Animation", "animationCreated", AnimationCreated.class),

  /**
   * Event for animation that has been started
   */
  AnimationAnimationStarted("Animation", "animationStarted", AnimationStarted.class),

  /**
   * Event for when an animation has been cancelled
   */
  AnimationAnimationCanceled("Animation", "animationCanceled", AnimationCanceled.class),

  /**
   * Issued when new message was logged
   */
  LogEntryAdded("Log", "entryAdded", EntryAdded.class),

  /**
   * Informs that port was successfully bound and got a specified connection id
   */
  TetheringAccepted("Tethering", "accepted", Accepted.class),

  /**
   * Issued when new execution context is created
   */
  RuntimeExecutionContextCreated("Runtime", "executionContextCreated", ExecutionContextCreated.class),

  /**
   * Issued when execution context is destroyed
   */
  RuntimeExecutionContextDestroyed("Runtime", "executionContextDestroyed", ExecutionContextDestroyed.class),

  /**
   * Issued when all executionContexts were cleared in browser
   */
  RuntimeExecutionContextsCleared("Runtime", "executionContextsCleared", ExecutionContextsCleared.class),

  /**
   * Issued when exception was thrown and unhandled
   */
  RuntimeExceptionThrown("Runtime", "exceptionThrown", ExceptionThrown.class),

  /**
   * Issued when unhandled exception was revoked
   */
  RuntimeExceptionRevoked("Runtime", "exceptionRevoked", ExceptionRevoked.class),

  /**
   * Issued when console API was called
   */
  RuntimeConsoleAPICalled("Runtime", "consoleAPICalled", ConsoleAPICalled.class),

  /**
   * Issued when object should be inspected (for example, as a result of inspect() command line API call)
   */
  RuntimeInspectRequested("Runtime", "inspectRequested", InspectRequested.class),

  /**
   * Fired when virtual machine parses script
   * This event is also fired for all known and uncollected scripts upon enabling debugger
   */
  DebuggerScriptParsed("Debugger", "scriptParsed", ScriptParsed.class),

  /**
   * Fired when virtual machine fails to parse the script
   */
  DebuggerScriptFailedToParse("Debugger", "scriptFailedToParse", ScriptFailedToParse.class),

  /**
   * Fired when breakpoint is resolved to an actual script and location
   */
  DebuggerBreakpointResolved("Debugger", "breakpointResolved", BreakpointResolved.class),

  /**
   * Fired when the virtual machine stopped on breakpoint or exception or any other stop criteria
   */
  DebuggerPaused("Debugger", "paused", Paused.class),

  /**
   * Fired when the virtual machine resumed execution
   */
  DebuggerResumed("Debugger", "resumed", Resumed.class),

  /**
   * Issued when new console message is added
   */
  ConsoleMessageAdded("Console", "messageAdded", MessageAdded.class),

  /**
   * Sent when new profile recodring is started using console
   * profile() call
   */
  ProfilerConsoleProfileStarted("Profiler", "consoleProfileStarted", ConsoleProfileStarted.class),

  ProfilerConsoleProfileFinished("Profiler", "consoleProfileFinished", ConsoleProfileFinished.class),

  HeapProfilerAddHeapSnapshotChunk("HeapProfiler", "addHeapSnapshotChunk", AddHeapSnapshotChunk.class),

  HeapProfilerResetProfiles("HeapProfiler", "resetProfiles", ResetProfiles.class),

  HeapProfilerReportHeapSnapshotProgress("HeapProfiler", "reportHeapSnapshotProgress", ReportHeapSnapshotProgress.class),

  /**
   * If heap objects tracking has been started then backend regulary sends a current value for last seen object id and corresponding timestamp
   * If the were changes in the heap since last event then one or more heapStatsUpdate events will be sent before a new lastSeenObjectId event
   */
  HeapProfilerLastSeenObjectId("HeapProfiler", "lastSeenObjectId", LastSeenObjectId.class),

  /**
   * If heap objects tracking has been started then backend may send update for one or more fragments
   */
  HeapProfilerHeapStatsUpdate("HeapProfiler", "heapStatsUpdate", HeapStatsUpdate.class);

  public final String domain;

  public final String name;

  public final Class<?> klass;

  Events(String domain, String name, Class<?> klass) {
    this.domain = domain;
    this.name = name;
    this.klass = klass;
  }

  @Override
  public String toString() {
    return domain + "." + name;
  }
}

