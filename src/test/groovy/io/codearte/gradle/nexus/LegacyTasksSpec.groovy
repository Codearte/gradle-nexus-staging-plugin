package io.codearte.gradle.nexus

import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.internal.project.ProjectInternal
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

class LegacyTasksSpec extends Specification {

    @Rule
    public TemporaryFolder tmpProjectDir = new TemporaryFolder()

    private Project project

    def setup() {
        project = ProjectBuilder.builder().withProjectDir(tmpProjectDir.root).build()
        project.apply(plugin: NexusStagingPlugin)
    }

    def "should execute new task (#newTaskName) on legacy task (#legacyTaskName) call"() {
        given:
            Task newTaskToDependOn = getJustOneTaskByNameOrFail(newTaskName)
        when:
            triggerEvaluate()
        then:
            getDependenciesForTaskWithName(legacyTaskName).contains(newTaskToDependOn)
        where:
            legacyTaskName              || newTaskName
            'promoteRepository'         || 'releaseRepository'
            'closeAndPromoteRepository' || 'closeAndReleaseRepository'
    }

    private void triggerEvaluate() {
        //Not available in public API, alternatively 'getJustOneTaskByNameOrFail("tasks")' could be used
        ((ProjectInternal) project).evaluate()
    }

    private Task getJustOneTaskByNameOrFail(String taskName) {
        Set<Task> tasks = project.getTasksByName(taskName, false) //forces "afterEvaluate"
        assert tasks?.size() == 1: "Expected tasks: '$taskName', All tasks: ${project.tasks}"
        return tasks[0]
    }

    private Set<Task> getDependenciesForTaskWithName(String taskName) {
        Task promoteRepositoryTask = project.tasks.getByName(taskName)
        return promoteRepositoryTask.taskDependencies.getDependencies(promoteRepositoryTask)
    }
}
