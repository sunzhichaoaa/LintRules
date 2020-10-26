package com.hsae.incrementlint

import com.android.tools.lint.HtmlReporter
import com.android.tools.lint.Reporter
import com.android.tools.lint.checks.BuiltinIssueRegistry
import com.android.tools.lint.client.api.IssueRegistry
import com.lintrules.IssuesRegister
import org.gradle.api.Plugin
import org.gradle.api.Project

public class IncrementLintPlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {
        System.out.println("====== increment lint check start123 ======")
        project.task("lintCheck").doLast {
            println("=========== Lint check start ==============")
            List<String> commitList = getCommitChange(project)

            List<File> files = new ArrayList<>()
            File file
            for (String s : commitList) {
                System.out.println("find file to commit:" + s)
                 file = new File(s)
                files.add(file)
            }

            def cl = new LintToolClient()
            def flag = cl.flags // LintCliFlags 用于设置Lint检查的一些标志
            flag.setExitCode = true

            /*
             * HtmlReport
             * 输出HTML格式的报告
             * 输出路径:/{$rootDir}/lint-all-result.html
             */
            Reporter reporter = new HtmlReporter(cl, new File("lint-result.html"), flag)
            flag.reporters.add(reporter)

            /*
             * 执行run方法开始lint检查
             *
             * LintIssueRegistry()-> 自定义Lint检查规则
             * files->需要检查的文件文件
             * result 检查结果 设置flag.setExitCode = true时, 有错误的时候返回1 反之返回0
             */
            println("------------ run start ------------")
            cl.run(new IssuesRegister(), files)
            println("------------- run end --------------")
            println("============ Lint check end ===============")
        }
    }

    /**
     * 通过Git命令获取需要检查的文件
     *
     * @param project gradle.Project
     * @return 文件名
     */
    static List<String> getCommitChange(Project project) {
        ArrayList<String> filterList = new ArrayList<>()
        try {
            //此命令获取本次提交的文件 在git commit之后执行
            System.out.println("project.getRootDir():" + project.getRootDir())
            String projectDir = project.getProjectDir()
            System.out.println("projectDir:" + projectDir)
            String cmdStatus = "git status -s"
            String cmdStatusINfo = cmdStatus.execute(null, project.getRootDir()).text.trim()
            System.out.println("cmdStatusINfo:" + cmdStatusINfo)
            //System.out.println("-------------------------------------------------------------------------")
            //String command = "git diff --name-only --diff-filter=ACMRTUXB"
            //String command = "git diff --name-only --diff-filter=ACMRTUXB  HEAD~1 HEAD~0 $projectDir"
            //System.out.println(command)
           //String changeInfo = command.execute(null, project.getRootDir()).text.trim()
            //System.out.println("changeINfo:" + changeInfo)
            if (cmdStatusINfo == null || cmdStatusINfo.empty) {
                return filterList
            }

            String[] lines = cmdStatusINfo.split("\\n")
            ArrayList<String> linesList = new ArrayList<>()
            linesList.clear();
            for (String line : lines) {
                if (line.contains("M ") || line.contains("src/") || line.endsWith(".java")) {
                    linesList.add(line.substring(line.lastIndexOf(" ") + 1));
                }
            }
            return linesList

/*            if (changeInfo == null || changeInfo.empty) {
                return filterList
            }

            String[] lines = changeInfo.split("\\n")

            return lines.toList()*/
        } catch (Exception e) {
            e.printStackTrace()
            return filterList
        }
    }
}