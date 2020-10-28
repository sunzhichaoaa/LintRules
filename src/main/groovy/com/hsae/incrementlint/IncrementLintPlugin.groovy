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

            IssueRegistry registry = new BuiltinIssueRegistry();

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
            cl.run(new IssuesRegister(), files)
            println("============ Lint check end ===============")
        }

        /*
        * gradle task: 将git hooks 脚本复制到.git/hooks文件夹下
        * 根据不同的系统类型复制不同的git hooks脚本(现支持Windows、Linux两种)
        */
        project.task("installGitHooks").doLast {
            println("OS Type:" + System.getProperty("os.name"))
            File postCommit
            String OSType = System.getProperty("os.name")
            System.out.println("windows:"+project.rootDir)
            if (OSType.contains("Windows")) {
                postCommit = new File(project.rootDir, "post-commit-windows")
            } else {
                postCommit = new File(project.rootDir, "post-commit")
            }

            project.copy {
                from (postCommit) {
                    rename {
                        String filename ->
                            "post-commit"
                    }
                }
                into new File(project.rootDir, ".git/hooks/")
            }
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

//    /**
//     * 通过Git命令获取需要检查的文件
//     *
//     * @param project gradle.Project
//     * @return 文件名
//     */
//    static List<String> getCommitChange(Project project) {
//        ArrayList<String> filterList = new ArrayList<>()
//        try {
//            //此命令获取本次提交的文件 在git commit之后执行
//            String projectDir = project.getProjectDir()
//            System.out.println("projectDir dir:"+project.getRootDir())
//            String versionCmd = "git diff .";
//            String versionInfo = versionCmd.execute(null, project.getRootDir()).text.trim()
//            System.out.println("print version cmd:"+versionCmd)
//            String command = "git diff --name-only --diff-filter=ACMRTUXB $projectDir"
//            System.out.println("root dir:"+project.getRootDir())
//            String changeInfo = command.execute(null, project.getRootDir()).text.trim()
//            System.out.println("getCommitChange:" + changeInfo)
//            if (changeInfo == null || changeInfo.empty) {
//                return filterList
//            }
//            String[] lines = changeInfo.split("\\n")
//            return lines.toList()
//        } catch (Exception e) {
//            e.printStackTrace()
//            return filterList
//        }
//    }

}