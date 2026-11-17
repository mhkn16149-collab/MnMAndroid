package com.mnm.downloader

import android.app.Activity
import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {
    private lateinit var downloadsList: ListView
    private lateinit var emptyText: TextView
    private lateinit var manager: DownloadManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        manager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val urlInput = findViewById<EditText>(R.id.urlInput)
        val downloadButton = findViewById<Button>(R.id.downloadButton)
        val refreshButton = findViewById<Button>(R.id.refreshButton)
        downloadsList = findViewById(R.id.downloadsList)
        emptyText = findViewById(R.id.emptyText)

        downloadButton.setOnClickListener {
            val raw = urlInput.text.toString().trim()
            val uri = runCatching { Uri.parse(raw) }.getOrNull()
            if (uri == null || uri.scheme !in listOf("http", "https") || uri.host.isNullOrBlank()) {
                Toast.makeText(this, "أدخل رابط HTTP أو HTTPS صحيح", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            try {
                val fileName = (uri.lastPathSegment ?: "mnm_download")
                    .substringAfterLast('/')
                    .replace(Regex("[^A-Za-z0-9._-]"), "_")
                    .take(100).ifBlank { "mnm_download" }
                val request = DownloadManager.Request(uri)
                    .setTitle(fileName)
                    .setDescription("جاري التنزيل بواسطة MnM")
                    .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                    .setAllowedOverMetered(true)
                    .setAllowedOverRoaming(false)
                    .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
                manager.enqueue(request)
                Toast.makeText(this, "بدأ التنزيل. راح يظهر بالقائمة بعد التحديث.", Toast.LENGTH_LONG).show()
                urlInput.text.clear()
                refreshDownloads()
            } catch (_: Exception) {
                Toast.makeText(this, "تعذّر بدء التنزيل. تحقق من الرابط.", Toast.LENGTH_LONG).show()
            }
        }

        refreshButton.setOnClickListener { refreshDownloads() }
        downloadsList.setOnItemClickListener { _, _, position, _ ->
            val item = downloadsList.getItemAtPosition(position) as String
            Toast.makeText(this, item, Toast.LENGTH_SHORT).show()
        }
        refreshDownloads()
    }

    private fun refreshDownloads() {
        val names = mutableListOf<String>()
        val cursor = manager.query(DownloadManager.Query().setFilterByStatus(
            DownloadManager.STATUS_PENDING or DownloadManager.STATUS_RUNNING or
                DownloadManager.STATUS_PAUSED or DownloadManager.STATUS_SUCCESSFUL or
                DownloadManager.STATUS_FAILED
        ))
        cursor?.use {
            val titleCol = it.getColumnIndex(DownloadManager.COLUMN_TITLE)
            val statusCol = it.getColumnIndex(DownloadManager.COLUMN_STATUS)
            val downloadedCol = it.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)
            val totalCol = it.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)
            while (it.moveToNext()) {
                val title = if (titleCol >= 0) it.getString(titleCol) ?: "ملف بدون اسم" else "ملف بدون اسم"
                val status = if (statusCol >= 0) it.getInt(statusCol) else -1
                val downloaded = if (downloadedCol >= 0) it.getLong(downloadedCol) else 0L
                val total = if (totalCol >= 0) it.getLong(totalCol) else -1L
                val state = when (status) {
                    DownloadManager.STATUS_SUCCESSFUL -> "مكتمل"
                    DownloadManager.STATUS_FAILED -> "فشل"
                    DownloadManager.STATUS_RUNNING -> "قيد التنزيل"
                    DownloadManager.STATUS_PENDING -> "بالانتظار"
                    DownloadManager.STATUS_PAUSED -> "متوقف مؤقتاً"
                    else -> "غير معروف"
                }
                val progress = if (total > 0) " — ${((downloaded * 100) / total).coerceIn(0, 100)}%" else ""
                names.add("$title\n$state$progress")
            }
        }
        downloadsList.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, names)
        val hasItems = names.isNotEmpty()
        downloadsList.visibility = if (hasItems) View.VISIBLE else View.GONE
        emptyText.visibility = if (hasItems) View.GONE else View.VISIBLE
    }
}
