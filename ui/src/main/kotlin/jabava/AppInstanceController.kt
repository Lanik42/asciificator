package jabava

import java.io.File

class AppInstanceController {

	companion object {

		private const val APPLICATION_STARTED = "appStart.cache"
	}

	/**
	 * Возвращает false если экземпляр приложения уже существует
	 */
	fun registerInstance(): Boolean {
		val currentAppInstancePid = ProcessHandle.current().pid().toString()

		if (isInstanceExist()) {
			return false
		} else {
			createCache(currentAppInstancePid)
			return true
		}
	}

	private fun isInstanceExist(): Boolean {
		if (File(APPLICATION_STARTED).exists()) {
			return checkProcessWithPidExist()
		} else {
			return false
		}
	}

	private fun checkProcessWithPidExist(): Boolean {
		return try {
			val savedPid = File(APPLICATION_STARTED).readText().toLong()

			//Проверяем есть ли процесс с сохраненным pid'ом
			ProcessHandle.allProcesses().anyMatch { it.pid() == savedPid }
		} catch (e: Exception) {
			false
		}
	}

	private fun createCache(appPid: String) {
		File(APPLICATION_STARTED).writeText(appPid)
	}

	fun unregisterInstance() {
		File(APPLICATION_STARTED).delete()
	}
}