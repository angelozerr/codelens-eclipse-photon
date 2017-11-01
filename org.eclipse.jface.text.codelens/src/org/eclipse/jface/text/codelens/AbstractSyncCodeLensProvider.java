package org.eclipse.jface.text.codelens;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.ITextViewer;

public abstract class AbstractSyncCodeLensProvider extends AbstractCodeLensProvider {

	@Override
	public CompletableFuture<List<? extends ICodeLens>> provideCodeLenses(ITextViewer viewer,
			IProgressMonitor monitor) {
		return CompletableFuture.supplyAsync(() -> {
			return provideSyncCodeLenses(viewer, monitor);
		});
	}

	@Override
	public CompletableFuture<ICodeLens> resolveCodeLens(ITextViewer viewer, ICodeLens codeLens,
			IProgressMonitor monitor) {
		return CompletableFuture.supplyAsync(() -> {
			return resolveSyncCodeLens(viewer, codeLens, monitor);
		});
	}

	protected abstract List<? extends ICodeLens> provideSyncCodeLenses(ITextViewer viewer, IProgressMonitor monitor);

	protected abstract ICodeLens resolveSyncCodeLens(ITextViewer viewer, ICodeLens codeLens, IProgressMonitor monitor);
}
