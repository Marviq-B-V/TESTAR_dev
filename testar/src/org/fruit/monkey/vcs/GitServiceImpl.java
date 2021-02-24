package org.fruit.monkey.vcs;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public class GitServiceImpl implements GitService {

    private static final String LOCAL_REPOSITORIES_PATH = "cloned";

    @Override
    public boolean cloneRepository(String repositoryUrl) {
        try {
            Git.cloneRepository()
                    .setURI(repositoryUrl)
                    .setDirectory(prepareRepositoryDirectory(repositoryUrl))
                    .call();
            return true;
        } catch (GitAPIException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean cloneRepository(String repositoryUrl, GitCredentials gitCredentials) {
        try {
            Git.cloneRepository()
                    .setURI(repositoryUrl)
                    .setDirectory(prepareRepositoryDirectory(repositoryUrl))
                    .setCredentialsProvider(new UsernamePasswordCredentialsProvider(gitCredentials.getUsername(), gitCredentials.getPassword()))
                    .call();
            return true;
        } catch (GitAPIException e) {
            e.printStackTrace();
            return false;
        }
    }

    private File prepareRepositoryDirectory(String repositoryUrl) {
        Path baseDirPath = Paths.get(System.getProperty("user.dir"))
                .getParent()
                .resolve(LOCAL_REPOSITORIES_PATH);
        Path repositoryPath = baseDirPath.resolve(extractRepositoryName(repositoryUrl));
        File repositoryDirectory = new File(repositoryPath.toAbsolutePath().toString());
        return repositoryDirectory;
    }

    private String extractRepositoryName(String repositoryUrl) {
        String[] urlElements = repositoryUrl.split("/");
        String repositoryNameWithExtension = urlElements[urlElements.length-1];
        String repositoryName = repositoryNameWithExtension.split("\\.")[0];
        return repositoryName;
    }
}
