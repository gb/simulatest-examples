package org.simulatest.example.springboot

import org.simulatest.environment.Environment
import org.simulatest.environment.annotation.EnvironmentParent
import org.springframework.beans.factory.annotation.Autowired

/**
 * World-state: **a fresh task list on day one** — 10 tasks on the board,
 * nothing marked done yet. The quiet moment before any work has started.
 *
 * Environment tree read as a sequence of world-states:
 *
 *   TaskListEnvironment              ◄── ROOT: fresh backlog, 10 tasks pending
 *     └── TaskListDayTwoEnvironment          partial progress: 5 done, 5 pending
 */
class TaskListEnvironment : Environment {

	@Autowired lateinit var tasks: TaskRepository

	override fun run() {
		tasks.saveAll(listOf(
			Task(title = "Set up CI pipeline"),
			Task(title = "Write unit tests"),
			Task(title = "Review pull request"),
			Task(title = "Fix login bug"),
			Task(title = "Update dependencies"),
			Task(title = "Design new dashboard"),
			Task(title = "Refactor auth module"),
			Task(title = "Add error logging"),
			Task(title = "Write API docs"),
			Task(title = "Deploy to staging")
		))
	}
}

/**
 * World-state: **the task list two days in** — five items checked off, five
 * still pending. Demonstrates JPA state inheritance: this environment mutates
 * rows its parent created, and the Insistence Layer still rolls the
 * mutations back cleanly between tests.
 */
@EnvironmentParent(TaskListEnvironment::class)
class TaskListDayTwoEnvironment : Environment {

	@Autowired lateinit var tasks: TaskRepository

	override fun run() {
		val done = setOf(
			"Set up CI pipeline",
			"Write unit tests",
			"Fix login bug",
			"Update dependencies",
			"Add error logging"
		)
		tasks.saveAll(
			tasks.findAll()
				.filter { it.title in done }
				.onEach { it.done = true }
		)
	}
}
