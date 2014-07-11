package org.moflon.tracing.sdm;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import org.eclipse.emf.ecore.EOperation;
import org.moflon.tracing.sdm.msgs.SetupDebuggingSessionMsg;
import org.moflon.tracing.sdm.msgs.TeardownDebuggingSessionMsg;
import org.moflon.tracing.sdm.states.Init;
import org.moflon.tracing.sdm.states.ProtocolState;
import org.moflon.tracing.sdm.states.Sending;

public class EADebuggingSocketTraceStrategy extends AbstractEaTraceStrategy
{

   public final static int PORT = 6423;

   public final static String LOCALHOST = "localhost";

   private static EADebuggingSocketTraceStrategy instance;

   private Socket comSocket;

   private BufferedReader in;

   private PrintWriter out;

   private ProtocolState state = Init.getInstance();

   public EADebuggingSocketTraceStrategy()
   {
      if (instance != null)
      {
         throw new IllegalStateException("Do not instantiate more than once!");
      }

   }

   @Override
   public void initializeStrategy()
   {
      try
      {
         comSocket = new Socket(LOCALHOST, PORT);
         out = new PrintWriter(comSocket.getOutputStream(), true);
         in = new BufferedReader(new InputStreamReader(comSocket.getInputStream()));

         SetupDebuggingSessionMsg msg = new SetupDebuggingSessionMsg();
         out.println(msg.toString());
         String response = in.readLine();
         if (response.equals(msg.toString()))
         {
            state = state.nextState(msg);
            state = state.nextState(null);
         }

         init(out);
         instance = this;
      } catch (UnknownHostException e)
      {
         // should not happen because "localhost" should exist
         throw new RuntimeException(e);
      } catch (IOException e)
      {
         throw new IllegalStateException(e);
      }
   }

   public static EADebuggingSocketTraceStrategy getInstance()
   {
      if (instance == null)
         instance = new EADebuggingSocketTraceStrategy();
      return instance;
   }

   public void teardownDebuggingSession()
   {
      if (state instanceof Sending)
      {
         TeardownDebuggingSessionMsg msg = new TeardownDebuggingSessionMsg();
         out.println(msg.toString());
         try
         {
            String response = in.readLine();
            if (response.equals(msg.toString()))
            {
               state = state.nextState(msg);
            }

         } catch (IOException e)
         {
            e.printStackTrace();
         } finally
         {
            out.close();

            try
            {
               in.close();
            } catch (IOException e)
            {
            }

            try
            {
               comSocket.close();
            } catch (IOException e)
            {
            }

            instance = null;
         }
      }
   }

   @Override
   protected void logOperationEnter(SDMTraceContext c, StackTraceWrapper stw, EOperation op, Object[] parameterValues)
   {
      if (state instanceof Sending)
         super.logOperationEnter(c, stw, op, parameterValues);
   }

   @Override
   protected void logOperationExit(SDMTraceContext c, StackTraceWrapper stw, EOperation op, Object result)
   {
      if (state instanceof Sending)
         super.logOperationExit(c, stw, op, result);
   }

   @Override
   protected void logPatternEnter(SDMTraceContext c, StackTraceWrapper stw, String storyPatternName, EOperation op)
   {
      if (state instanceof Sending)
         super.logPatternEnter(c, stw, storyPatternName, op);
   }

   @Override
   protected void logPatternExit(SDMTraceContext c, StackTraceWrapper stw, String storyPatternName, EOperation op)
   {
      if (state instanceof Sending)
         super.logPatternExit(c, stw, storyPatternName, op);
   }

   @Override
   protected void logBindObjVar(SDMTraceContext c, StackTraceWrapper stw, String objVarName, Class<?> objVarType, Object oldValue, Object newValue)
   {
      if (state instanceof Sending)
         super.logBindObjVar(c, stw, objVarName, objVarType, oldValue, newValue);
   }

   @Override
   protected void logUnbindObjVar(SDMTraceContext c, StackTraceWrapper stw, String objVarName, Class<?> objVarType, Object oldValue, Object newValue)
   {
      if (state instanceof Sending)
         super.logUnbindObjVar(c, stw, objVarName, objVarType, oldValue, newValue);
   }

   @Override
   protected void logMatchFound(SDMTraceContext c, StackTraceWrapper stw, String storyPatternName, EOperation op, Object... paramValues)
   {
      if (state instanceof Sending)
         super.logMatchFound(c, stw, storyPatternName, op, paramValues);
   }

   @Override
   protected void logNoMatchFound(SDMTraceContext c, StackTraceWrapper stw, String storyPatternName, EOperation op, Object... paramValues)
   {
      if (state instanceof Sending)
         super.logNoMatchFound(c, stw, storyPatternName, op, paramValues);
   }

   @Override
   protected void logCheckIsomorphicBindingEvent(SDMTraceContext c, StackTraceWrapper stw, String objVar1Name, Class<?> objVar1Type, Object objVar1Value,
         String objVar2Name, Class<?> objVar2Type, Object objVar2Value)
   {
      if (state instanceof Sending)
         super.logCheckIsomorphicBindingEvent(c, stw, objVar1Name, objVar1Type, objVar1Value, objVar2Name, objVar2Type, objVar2Value);
   }

   @Override
   protected void logSuccessIsomorphicBindingEvent(SDMTraceContext c, StackTraceWrapper stw, String objVar1Name, Class<?> objVar1Type, Object objVar1Value,
         String objVar2Name, Class<?> objVar2Type, Object objVar2Value)
   {
      if (state instanceof Sending)
         super.logSuccessIsomorphicBindingEvent(c, stw, objVar1Name, objVar1Type, objVar1Value, objVar2Name, objVar2Type, objVar2Value);
   }

   @Override
   protected void logFailedIsomorphicBinding(SDMTraceContext c, StackTraceWrapper stw, String objVar1Name, Class<?> objVar1Type, Object objVar1Value,
         String objVar2Name, Class<?> objVar2Type, Object objVar2Value)
   {
      if (state instanceof Sending)
         super.logFailedIsomorphicBinding(c, stw, objVar1Name, objVar1Type, objVar1Value, objVar2Name, objVar2Type, objVar2Value);
   }

   @Override
   protected void logNoMoreLinkEndOptions(SDMTraceContext c, StackTraceWrapper stw, String linkName, String srcObjName, String trgtObjName)
   {
      if (state instanceof Sending)
         super.logNoMoreLinkEndOptions(c, stw, linkName, srcObjName, trgtObjName);
   }

   @Override
   protected void logObjectCreation(SDMTraceContext c, StackTraceWrapper stw, String objVarName, Class<?> objVarType, Object newObjectValue)
   {
      if (state instanceof Sending)
         super.logObjectCreation(c, stw, objVarName, objVarType, newObjectValue);
   }

   @Override
   protected void logObjectDeletion(SDMTraceContext c, StackTraceWrapper stw, String objVarName, Class<?> objVarType, Object oldObjectValue)
   {
      if (state instanceof Sending)
         super.logObjectDeletion(c, stw, objVarName, objVarType, oldObjectValue);
   }

   @Override
   protected void logLinkCreation(SDMTraceContext c, StackTraceWrapper stw, String sourceNodeName, Class<?> sourceNodeType, Object sourceNodeValue,
         String sourceRoleName, String targetNodeName, Class<?> targetNodeType, Object targetNodeValue, String targetRoleName)
   {
      if (state instanceof Sending)
         super.logLinkCreation(c, stw, sourceNodeName, sourceNodeType, sourceNodeValue, sourceRoleName, targetNodeName, targetNodeType, targetNodeValue,
               targetRoleName);
   }

   @Override
   protected void logLinkDeletion(SDMTraceContext c, StackTraceWrapper stw, String sourceRoleName, Class<?> sourceNodeType, Object sourceNodeValue,
         String sourceNodeName, String targetNodeName, Class<?> targetNodeType, Object targetNodeValue, String targetRoleName)
   {
      if (state instanceof Sending)
         super.logLinkDeletion(c, stw, sourceRoleName, sourceNodeType, sourceNodeValue, sourceNodeName, targetNodeName, targetNodeType, targetNodeValue,
               targetRoleName);
   }

   @Override
   protected void logLightweightPatternEnter(SDMTraceContext c, StackTraceWrapper stw, String storyPatternName, EOperation op, String uniqueId)
   {
      if (state instanceof Sending)
         super.logLightweightPatternEnter(c, stw, storyPatternName, op, uniqueId);
   }

   @Override
   protected void logLightweightPatternExit(SDMTraceContext c, StackTraceWrapper stw, String storyPatternName, EOperation op, String uniqueId)
   {
      if (state instanceof Sending)
         super.logLightweightPatternExit(c, stw, storyPatternName, op, uniqueId);
   }

   @Override
   protected void logCommenceOfGraphRewriting(SDMTraceContext c, StackTraceWrapper stw, String patternName)
   {
      if (state instanceof Sending)
         super.logCommenceOfGraphRewriting(c, stw, patternName);
   }

   @Override
   protected void logBeginNACEvaluation(SDMTraceContext c, StackTraceWrapper stw, String patternName)
   {
      if (state instanceof Sending)
         super.logBeginNACEvaluation(c, stw, patternName);
   }

   @Override
   protected void logEndOfNACEvaluation(SDMTraceContext c, StackTraceWrapper stw, String patternName)
   {
      if (state instanceof Sending)
         super.logEndOfNACEvaluation(c, stw, patternName);
   }

   @Override
   protected void logNACNotSatisfied(SDMTraceContext c, StackTraceWrapper stw, String patternName)
   {
      if (state instanceof Sending)
         super.logNACNotSatisfied(c, stw, patternName);
   }

   @Override
   protected void logNACSatisfied(SDMTraceContext c, StackTraceWrapper stw, String patternName)
   {
      if (state instanceof Sending)
         super.logNACSatisfied(c, stw, patternName);
   }

}
