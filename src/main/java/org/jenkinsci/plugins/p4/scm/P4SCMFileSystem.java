package org.jenkinsci.plugins.p4.scm;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.model.Item;
import hudson.scm.SCM;
import hudson.util.LogTaskListener;
import jenkins.scm.api.SCMFile;
import jenkins.scm.api.SCMFileSystem;
import jenkins.scm.api.SCMRevision;
import jenkins.scm.api.SCMSource;
import org.jenkinsci.plugins.p4.PerforceScm;
import org.jenkinsci.plugins.p4.client.TempClientHelper;
import org.jenkinsci.plugins.p4.workspace.Workspace;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class P4SCMFileSystem extends SCMFileSystem {

	private static final Logger LOGGER = Logger.getLogger(P4SCMFileSystem.class.getName());

	private TempClientHelper p4;

	protected P4SCMFileSystem(@NonNull Item owner, @NonNull PerforceScm scm, @CheckForNull P4Revision rev) throws Exception {
		super(rev);
		String credential = scm.getCredential();
		LogTaskListener listener = new LogTaskListener(LOGGER, Level.ALL);
		Workspace workspace = scm.getWorkspace();
		this.p4 = new TempClientHelper(owner, credential, listener, workspace);
	}

	@Override
	public void close() throws IOException {
		p4.close();
	}

	@Override
	public long lastModified() throws IOException, InterruptedException {
		return 0;
	}

	@NonNull
	@Override
	public SCMFile getRoot() {
		return new P4SCMFile(this);
	}

	@Extension
	public static class BuilderImpl extends SCMFileSystem.Builder {

		@Override
		public boolean supports(SCM source) {
			if (source instanceof PerforceScm) {
				return true;
			}
			return false;
		}

		@Override
		public boolean supports(SCMSource source) {
			if (source instanceof AbstractP4ScmSource) {
				return true;
			}
			return false;
		}

		@Override
		public SCMFileSystem build(@NonNull Item owner, @NonNull SCM scm, @CheckForNull SCMRevision rev) throws IOException, InterruptedException {
			if (scm == null || !(scm instanceof PerforceScm)) {
				return null;
			}
			PerforceScm p4scm = (PerforceScm) scm;

			if (rev != null && !(rev instanceof P4Revision)) {
				return null;
			}
			P4Revision p4rev = (P4Revision) rev;

			try {
				return new P4SCMFileSystem(owner, p4scm, p4rev);
			} catch (Exception e) {
				throw new IOException(e);
			}
		}
	}

	public TempClientHelper getConnection() {
		return p4;
	}
}
