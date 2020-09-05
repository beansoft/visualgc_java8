// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.github.beansoft.devkit.util

import com.intellij.openapi.project.Project
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiClass
import com.intellij.psi.SmartPointerManager
import com.intellij.psi.search.PsiSearchHelper
import com.intellij.psi.search.UsageSearchContext
import com.intellij.psi.util.ClassUtil
import com.intellij.psi.xml.XmlTag
import com.intellij.util.SmartList
import com.intellij.util.xml.DomManager
import org.jetbrains.idea.devkit.dom.Action
import org.jetbrains.idea.devkit.dom.ExtensionPoint
import org.jetbrains.idea.devkit.util.ExtensionCandidate
import org.jetbrains.idea.devkit.util.PluginRelatedLocatorsUtils
import java.util.*

fun locateActionsByPsiClass(psiClass: PsiClass): List<ExtensionCandidate> {
  return findActionsByClassName(psiClass.project, ClassUtil.getJVMClassName(psiClass) ?: return emptyList())
}


// Read for Action
internal fun processActionDeclarations(name: String, project: Project, strictMatch: Boolean = true, callback: (Action, XmlTag) -> Boolean) {
  val scope = PluginRelatedLocatorsUtils.getCandidatesScope(project)
  PsiSearchHelper.getInstance(project).processElementsWithWord(
          { element, offsetInElement ->
            val elementAtOffset = (element as? XmlTag)?.findElementAt(offsetInElement) ?: return@processElementsWithWord true
            if (strictMatch) {
              if (!elementAtOffset.textMatches(name)) {
                return@processElementsWithWord true
              }
            }
            else if (!StringUtil.contains(elementAtOffset.text, name)) {
              return@processElementsWithWord true
            }

            val extension = DomManager.getDomManager(project).getDomElement(element) as? Action ?: return@processElementsWithWord true
            callback(extension, element)
          }, scope, name, UsageSearchContext.IN_FOREIGN_LANGUAGES, /* case-sensitive = */ true)
}


private fun findActionsByClassName(project: Project, className: String): List<ExtensionCandidate> {
  val result = Collections.synchronizedList(SmartList<ExtensionCandidate>())
  val smartPointerManager by lazy { SmartPointerManager.getInstance(project) }
  processActionsByClassName(project, className) { tag, _ ->
    result.add(ExtensionCandidate(smartPointerManager.createSmartPsiElementPointer(tag)))
    true
  }
  return result
}

internal inline fun processActionsByClassName(project: Project, className: String, crossinline processor: (XmlTag, Action) -> Boolean) {
  processActionDeclarations(className, project) { action, tag ->
      action?.let { processor(tag, it) } ?: true
  }
}
