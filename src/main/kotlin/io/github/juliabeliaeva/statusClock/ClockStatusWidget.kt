package io.github.juliabeliaeva.statusClock

import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.wm.StatusBar
import com.intellij.openapi.wm.StatusBarWidget
import com.intellij.openapi.wm.StatusBarWidgetFactory
import com.intellij.util.Consumer
import com.intellij.util.concurrency.AppExecutorUtil
import com.intellij.util.text.DateFormatUtil
import com.intellij.util.text.JBDateFormat
import java.awt.Component
import java.awt.event.MouseEvent
import java.util.concurrent.TimeUnit

private const val ID = "StatusClock"

class ClockStatusWidget : StatusBarWidget, StatusBarWidget.TextPresentation {
    private var statusBar: StatusBar? = null

    init {
        val future = AppExecutorUtil.getAppScheduledExecutorService().scheduleWithFixedDelay({
            runInEdt(this) { statusBar?.updateWidget(ID) }
        }, 0, 1, TimeUnit.SECONDS)
        Disposer.register(this, Disposable { future.cancel(false) })
    }

    override fun ID(): String = ID
    override fun getPresentation(): StatusBarWidget.WidgetPresentation? = this
    override fun getTooltipText(): String? = JBDateFormat.getFormatter().formatDateTime(System.currentTimeMillis())
    override fun getText(): String = DateFormatUtil.formatTime(System.currentTimeMillis())
    override fun getClickConsumer(): Consumer<MouseEvent>? = null
    override fun getAlignment(): Float = Component.CENTER_ALIGNMENT

    override fun install(statusBar: StatusBar) {
        this.statusBar = statusBar
    }

    override fun dispose() {
        statusBar = null
    }
}

class ClockStatusBarWidgetFactory : StatusBarWidgetFactory {
    override fun getId(): String = ID
    override fun getDisplayName(): String = "Status Bar Clock"
    override fun isAvailable(project: Project): Boolean = true
    override fun canBeEnabledOn(statusBar: StatusBar): Boolean = true
    override fun createWidget(project: Project): StatusBarWidget = ClockStatusWidget()
    override fun disposeWidget(widget: StatusBarWidget) {
        if (widget.ID() == ID) Disposer.dispose(widget)
    }
}

private fun runInEdt(disposable: Disposable, action: () -> Unit) {
    ApplicationManager.getApplication().invokeLater(action, { Disposer.isDisposed(disposable) })
}